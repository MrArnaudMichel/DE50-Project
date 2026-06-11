package fr.utbm.svn.service.strategy;

import com.telelogic.rhapsody.core.IRPModelElement;
import fr.utbm.svn.Logger;
import fr.utbm.svn.model.*;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.*;

public class ValueLoopStrategy implements ICalculationStrategy {

    private final Logger logger = Logger.getInstance();

    @Override
    public Map<Stakeholder, Double> computeScores(List<Stakeholder> stakeholders, List<ValueArc> valueArcs, SVNSystem svnSystem) {

        // Construit le graphe orienté : nom → liste de (voisin, arc)
        Map<String, List<ValueArc>> graph = this.buildGraph(valueArcs);

        // Trouve tous les cycles passant par le système
        List<ValueLoop> loops = findValueLoops(svnSystem.getName(), graph);
        logger.log("Value loops trouvés : " + loops.size());

        if (loops.isEmpty()) {
            logger.log("Aucun loop — calcul simplifié par somme des arcs.");
            return Collections.emptyMap();
        }

        // Calcule le score de chaque loop (produit des arcs)
        double totalLoopScore = 0;
        for (ValueLoop loop : loops) {
            loop.setScore(1);
            for (double s : loop.getArcScores()) loop.setScore(loop.getScore() * s);
            totalLoopScore += loop.getScore();
            logger.log("Loop " + loop.getNodes() + " score=" + loop.getScore());
        }

        // Calcule l'importance de chaque stakeholder (Équation 2)
        for (Stakeholder sh : stakeholders) {
            double sumLoopsContaining = 0;
            for (ValueLoop loop : loops) {
                if (loop.getNodes().contains(sh.getName())) {
                    sumLoopsContaining += loop.getScore();
                }
            }
            double importance = (totalLoopScore > 0) ? sumLoopsContaining / totalLoopScore : 0;
            sh.setScore(importance);
            logger.log("Importance " + sh.getName()
                    + " = " + String.format("%.4f", importance));
        }

        Map<Stakeholder, Double> scores = new HashMap<>();

        for (Stakeholder sh : stakeholders) {
            scores.put(sh, sh.getScore());
        }

        svnSystem.setTotalLoopScore(totalLoopScore);
        logger.log("Calcul terminé. " + stakeholders.size() + " acteurs mis à jour.");

        return scores;
    }

    private Map<String, List<ValueArc>> buildGraph(List<ValueArc> arcs) {
        Map<String, List<ValueArc>> graph = new HashMap<>();
        for (ValueArc arc : arcs) {
            try {
                IRPModelElement dependent = arc.getDependent();
                IRPModelElement dependsOn = arc.getDependsOn();
                if (dependent == null || dependsOn == null) continue;

                graph.computeIfAbsent(dependent.getName(), k -> new ArrayList<>())
                        .add(arc);

            } catch (Exception ignored) {}
        }
        return graph;
    }

    private List<ValueLoop> findValueLoops(String systemName,
                                           Map<String, List<ValueArc>> graph) {
        List<ValueLoop> result = new ArrayList<>();
        if (!graph.containsKey(systemName)) return result;

        Deque<SearchState> stack = new ArrayDeque<>();
        stack.push(new SearchState(systemName, new ArrayList<>(),
                new ArrayList<>(), new HashSet<>()));

        while (!stack.isEmpty()) {
            SearchState state = stack.pop();

            List<ValueArc> neighbors = graph.getOrDefault(state.getCurrent(), Collections.emptyList());
            for (ValueArc edge : neighbors) {
                String next = edge.getDependsOn().getName();

                // Boucle fermée : on est revenu au système
                if (next.equals(systemName) && !state.getPath().isEmpty()) {
                    List<String> loopNode = new ArrayList<>(state.getPath());
                    loopNode.add(systemName);
                    List<Double> loopScores = new ArrayList<>(state.getScores());
                    loopScores.add(edge.getScore());
                    ValueLoop loop = new ValueLoop(loopNode, loopScores);
                    result.add(loop);
                    continue;
                }

                // Évite les cycles infinis
                if (state.getVisited().contains(next)) continue;

                Set<String> newVisited = new HashSet<>(state.getVisited());
                newVisited.add(next);
                List<String> newPath = new ArrayList<>(state.getPath());
                newPath.add(next);
                List<Double> newScores = new ArrayList<>(state.getScores());
                newScores.add(edge.getScore());

                stack.push(new SearchState(next, newPath, newScores, newVisited));
            }
        }
        return result;
    }
}
