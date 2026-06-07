package fr.utbm.RhapsodySVN.service;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;

import java.util.*;

import static fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper.getTagValue;

public class CalculationService {

    /**
     * Matrice de score des arcs (Figure 3, INCOSE 2018).
     * Score = f(BenefitRanking, SupplyImportance)
     *
     *              Might Be  Should Be  Must Be
     * High           0.3       0.5       0.95
     * Medium         0.2       0.4       0.8
     * Low            0.1       0.2       0.4
     */
    public static double getArcScore(String benefit, String supply) {
        if ("MUST_BE".equals(benefit)) {
            if ("HIGH".equals(supply))   return 0.95;
            if ("MEDIUM".equals(supply)) return 0.8;
            if ("LOW".equals(supply))    return 0.4;
        } else if ("SHOULD_BE".equals(benefit)) {
            if ("HIGH".equals(supply))   return 0.5;
            if ("MEDIUM".equals(supply)) return 0.4;
            if ("LOW".equals(supply))    return 0.2;
        } else { // MIGHT_BE ou non défini
            if ("HIGH".equals(supply))   return 0.3;
            if ("MEDIUM".equals(supply)) return 0.2;
            if ("LOW".equals(supply))    return 0.1;
        }
        return 0.2;
    }



    public void calculateImportance(IRPModelElement root) {
        System.out.println("[SVN] Début du calcul d'importance pour : " + root.getName());

        List<IRPActor> stakeholders = findStakeholders(root);
        if (stakeholders.isEmpty()) {
            System.out.println("[SVN] Aucun stakeholder trouvé.");
            return;
        }

        List<IRPDependency> allArcs = findValueArcs(root);
        System.out.println("[SVN] ValueArcs trouvés : " + allArcs.size());
        if (allArcs.isEmpty()) {
            System.out.println("[SVN] Aucun arc — calcul impossible.");
            return;
        }

        IRPModelElement system = findSystem(root);
        if (system == null) {
            System.out.println("[SVN] Aucun nœud «system» trouvé — "
                    + "calcul simplifié par somme des arcs.");
            calculateByArcSum(stakeholders, allArcs);
            return;
        }
        System.out.println("[SVN] Système central : " + system.getName());

        // Calcul par value loops (Équations 1 & 2)
        calculateByValueLoops(stakeholders, allArcs, system);
    }

    // -------------------------------------------------------------------------
    // Calcul par value loops (méthode correcte INCOSE 2018)
    // -------------------------------------------------------------------------

    /**
     * Équation 1 : score d'un value loop = produit des scores de ses arcs.
     * Équation 2 : importance(stakeholder) = Σ loops contenant S / Σ tous loops.
     *
     * Un value loop commence et finit sur le nœud «system».
     */
    private void calculateByValueLoops(List<IRPActor> stakeholders,
                                       List<IRPDependency> allArcs,
                                       IRPModelElement system) {
        // Construit le graphe orienté : nom → liste de (voisin, arc)
        Map<String, List<ArcEdge>> graph = buildGraph(allArcs);

        // Trouve tous les cycles passant par le système
        List<ValueLoop> loops = findValueLoops(system.getName(), graph);
        System.out.println("[SVN] Value loops trouvés : " + loops.size());

        if (loops.isEmpty()) {
            System.out.println("[SVN] Aucun loop — calcul simplifié par somme des arcs.");
            calculateByArcSum(stakeholders, allArcs);
            return;
        }

        // Calcule le score de chaque loop (produit des arcs)
        double totalLoopScore = 0;
        for (ValueLoop loop : loops) {
            loop.score = 1.0;
            for (double s : loop.arcScores) loop.score *= s;
            totalLoopScore += loop.score;
            System.out.println("[SVN] Loop " + loop.nodes + " score=" + loop.score);
        }

        // Calcule l'importance de chaque stakeholder (Équation 2)
        List<StakeholderScore> scores = new ArrayList<>();
        for (IRPActor sh : stakeholders) {
            double sumLoopsContaining = 0;
            for (ValueLoop loop : loops) {
                if (loop.nodes.contains(sh.getName())) {
                    sumLoopsContaining += loop.score;
                }
            }
            double importance = (totalLoopScore > 0) ? sumLoopsContaining / totalLoopScore : 0;
            scores.add(new StakeholderScore(sh, importance));
            System.out.println("[SVN] Importance " + sh.getName()
                    + " = " + String.format("%.4f", importance));
        }

        for (StakeholderScore ss : scores) {
            updateImportanceTag(ss.element, ss.score);
        }
        updateSystemTags(system, loops, totalLoopScore);
        System.out.println("[SVN] Calcul terminé. " + stakeholders.size() + " acteurs mis à jour.");
    }

    /**
     * Trouve tous les cycles qui commencent et finissent sur le nœud système,
     * en traversant au maximum tous les nœuds une fois (DFS).
     */
    private List<ValueLoop> findValueLoops(String systemName,
                                           Map<String, List<ArcEdge>> graph) {
        List<ValueLoop> result = new ArrayList<>();
        if (!graph.containsKey(systemName)) return result;

        Deque<SearchState> stack = new ArrayDeque<>();
        stack.push(new SearchState(systemName, new ArrayList<>(),
                new ArrayList<>(), new HashSet<>()));

        while (!stack.isEmpty()) {
            SearchState state = stack.pop();

            List<ArcEdge> neighbors = graph.getOrDefault(state.current, Collections.emptyList());
            for (ArcEdge edge : neighbors) {
                String next = edge.target;

                // Boucle fermée : on est revenu au système
                if (next.equals(systemName) && !state.path.isEmpty()) {
                    List<String> loopNodes = new ArrayList<>(state.path);
                    loopNodes.add(systemName);
                    List<Double> loopScores = new ArrayList<>(state.scores);
                    loopScores.add(edge.score);
                    ValueLoop loop = new ValueLoop(loopNodes, loopScores);
                    result.add(loop);
                    continue;
                }

                // Évite les cycles infinis
                if (state.visited.contains(next)) continue;

                Set<String> newVisited = new HashSet<>(state.visited);
                newVisited.add(next);
                List<String> newPath = new ArrayList<>(state.path);
                newPath.add(next);
                List<Double> newScores = new ArrayList<>(state.scores);
                newScores.add(edge.score);

                stack.push(new SearchState(next, newPath, newScores, newVisited));
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Calcul simplifié par somme des arcs (fallback sans nœud système)
    // -------------------------------------------------------------------------

    private void calculateByArcSum(List<IRPActor> stakeholders, List<IRPDependency> allArcs) {
        List<StakeholderScore> scores = new ArrayList<>();
        double total = 0;

        for (IRPActor sh : stakeholders) {
            double score = 0;
            for (IRPDependency arc : allArcs) {
                try {
                    IRPModelElement end1 = arc.getDependent();
                    IRPModelElement end2 = arc.getDependsOn();
                    if ((end1 != null && sh.getName().equals(end1.getName()))
                            || (end2 != null && sh.getName().equals(end2.getName()))) {
                        score += getArcScore(arc);
                    }
                } catch (Exception ignored) {}
            }
            scores.add(new StakeholderScore(sh, score));
            total += score;
        }

        for (StakeholderScore ss : scores) {
            double importance = (total > 0) ? ss.score / total : 0;
            updateImportanceTag(ss.element, importance);
            System.out.println("[SVN] Importance (simplifié) " + ss.element.getName()
                    + " = " + String.format("%.4f", importance));
        }
    }

    // -------------------------------------------------------------------------
    // Construction du graphe
    // -------------------------------------------------------------------------

    private Map<String, List<ArcEdge>> buildGraph(List<IRPDependency> arcs) {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        for (IRPDependency arc : arcs) {
            try {
                IRPModelElement end1 = arc.getDependent();
                IRPModelElement end2 = arc.getDependsOn();
                if (end1 == null || end2 == null) continue;

                double score = getArcScore(arc);
                graph.computeIfAbsent(end1.getName(), k -> new ArrayList<>())
                        .add(new ArcEdge(end2.getName(), score));

                // Si non-dirigé, ajouter aussi le sens inverse
                // (les flows Rhapsody sont dirigés via Direction)
            } catch (Exception ignored) {}
        }
        return graph;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private double getArcScore(IRPDependency arc) {
        String benefit = getTagValue(arc, SVNConstants.TAG_BENEFIT_RANKING, "?");
        String supply  = getTagValue(arc, SVNConstants.TAG_SUPPLY_IMPORTANCE, "?");
        return getArcScore(benefit, supply);
    }

    private List<IRPActor> findStakeholders(IRPModelElement root) {
        List<IRPActor> result = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPActor
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_STAKEHOLDER)) {
                result.add((IRPActor) el);
                System.out.println("[SVN] Stakeholder trouvé : " + el.getName());
            }
        }
        return result;
    }

    private List<IRPDependency> findValueArcs(IRPModelElement root) {
        List<IRPDependency> result = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPDependency
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_VALUE_ARC)) {
                result.add((IRPDependency) el);
            }
        }
        return result;
    }

    private IRPModelElement findSystem(IRPModelElement root) {
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_SYSTEM)) {
                return el;
            }
        }
        return null;
    }

    private void updateImportanceTag(IRPModelElement el, double score) {
        IRPTag tag = el.getTag(SVNConstants.TAG_IMPORTANCE_SCORE);
        if (tag == null) {
            try { tag = (IRPTag) el.addNewAggr("Tag", SVNConstants.TAG_IMPORTANCE_SCORE); }
            catch (Exception ignored) {}
        }
        if (tag != null) {
            tag.setValue(String.format("%.4f", score));
            el.setDisplayName(el.getName()+" : "+String.format("%.4f", score));
        }
    }

    private void updateSystemTags(IRPModelElement system,
                                  List<ValueLoop> loops,
                                  double totalLoopScore) {
        setOrCreateTag(system, "totalLoopScore",
                String.format("%.4f", totalLoopScore));
        setOrCreateTag(system, "loopCount",
                String.valueOf(loops.size()));

        // Optionnel : détail des loops sous forme lisible
        StringBuilder detail = new StringBuilder();
        for (ValueLoop loop : loops) {
            detail.append(loop.nodes.toString())
                    .append("=")
                    .append(String.format("%.4f", loop.score))
                    .append("; ");
        }
        setOrCreateTag(system, "loopDetails", detail.toString());
    }

    private void setOrCreateTag(IRPModelElement el, String tagName, String value) {
        try {
            IRPTag tag = el.getTag(tagName);
            if (tag == null) {
                tag = (IRPTag) el.addNewAggr("Tag", tagName);
            }
            if (tag != null) tag.setValue(value);
        } catch (Exception e) {
            System.err.println("[SVN] setOrCreateTag " + tagName + " : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Classes internes
    // -------------------------------------------------------------------------

    private static class ArcEdge {
        final String target;
        final double score;
        ArcEdge(String t, double s) { target = t; score = s; }
    }

    private static class ValueLoop {
        final List<String> nodes;
        final List<Double> arcScores;
        double score;
        ValueLoop(List<String> n, List<Double> s) { nodes = n; arcScores = s; }
    }

    private static class SearchState {
        final String current;
        final List<String> path;
        final List<Double> scores;
        final Set<String> visited;
        SearchState(String c, List<String> p, List<Double> s, Set<String> v) {
            current = c; path = p; scores = s; visited = v;
        }
    }

    private static class StakeholderScore {
        final IRPActor element;
        final double score;
        StakeholderScore(IRPActor el, double s) { element = el; score = s; }
    }
}