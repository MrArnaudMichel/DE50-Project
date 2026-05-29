package fr.utbm.RhapsodySVN.service;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Service utilitaire pour les opérations graphiques sur les diagrammes SVN.
 */
public class DiagramService {

    // -------------------------------------------------------------------------
    // 1. Labels sur les arcs
    // -------------------------------------------------------------------------

    /**
     * Pour chaque «valuearc» trouvé sous root, met à jour le label graphique
     * "B:<benefitRanking> | S:<supplyImportance>" sur toutes ses représentations
     * dans tous les diagrammes du projet.
     */
    public void updateArcLabels(IRPModelElement root) {
        System.out.println("[SVN] Mise à jour des labels d'arcs pour : " + root.getName());

        List<IRPRelation> arcs = findValueArcs(root);
        if (arcs.isEmpty()) {
            System.out.println("[SVN] Aucun valuearc trouvé.");
            return;
        }

        // Collecte tous les diagrammes du projet
        List<IRPObjectModelDiagram> diagrams = findSVNDiagrams(root);

        int updated = 0;
        for (IRPRelation arc : arcs) {
            String label = buildArcLabel(arc);
            for (IRPObjectModelDiagram diagram : diagrams) {
                IRPCollection graphElements = diagram.getGraphicalElements();
                for (int i = 1; i <= graphElements.getCount(); i++) {
                    Object item = graphElements.getItem(i);
                    if (item instanceof IRPGraphElement) {
                        IRPGraphElement ge = (IRPGraphElement) item;
                        IRPModelElement model = ge.getModelObject();
                        if (arc.equals(model)) {
                            try {
                                // "Label" : textName valide pour les lignes (cf. Javadoc IRPGraphElement)
                                ge.setGraphicalPropertyOfText("Label", "Text", label);
                                updated++;
                            } catch (Exception e) {
                                System.err.println("[SVN] Label arc '" + arc.getName() + "' : " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("[SVN] Labels mis à jour sur " + updated + " représentation(s).");
    }

    private String buildArcLabel(IRPRelation arc) {
        String benefit = getTagValue(arc, SVNConstants.TAG_BENEFIT_RANKING, "?");
        String supply  = getTagValue(arc, SVNConstants.TAG_SUPPLY_IMPORTANCE, "?");
        return "B:" + benefit + " | S:" + supply;
    }

    // -------------------------------------------------------------------------
    // 2. Couleur d'un arc sélectionné
    // -------------------------------------------------------------------------

    /**
     * Change la couleur de ligne (LineColor) de toutes les représentations graphiques
     * du «valuearc» donné dans tous les diagrammes SVN.
     *
     * @param modelElement  IRPRelation «valuearc» sélectionné
     * @param hexColor      couleur RGB hex sans '#', ex. "FF0000"
     */
    public void setArcColor(IRPModelElement modelElement, String hexColor) {
        if (!(modelElement instanceof IRPRelation)) {
            System.err.println("[SVN] setArcColor : l'élément sélectionné n'est pas une relation.");
            return;
        }
        IRPRelation arc = (IRPRelation) modelElement;
        if (!RhapsodyWrapper.hasStereotype(arc, SVNConstants.STEREOTYPE_VALUE_ARC)) {
            System.err.println("[SVN] setArcColor : la relation n'est pas un «valuearc».");
            return;
        }

        String color = hexColor.replace("#", "").toUpperCase();
        int changed = 0;

        List<IRPObjectModelDiagram> diagrams = findSVNDiagrams(arc.getOwner());
        for (IRPObjectModelDiagram diagram : diagrams) {
            IRPCollection graphElements = diagram.getGraphicalElements();
            for (int i = 1; i <= graphElements.getCount(); i++) {
                Object item = graphElements.getItem(i);
                if (item instanceof IRPGraphElement) {
                    IRPGraphElement ge = (IRPGraphElement) item;
                    if (arc.equals(ge.getModelObject())) {
                        try {
                            ge.setGraphicalProperty("LineColor", color);
                            changed++;
                        } catch (Exception e) {
                            System.err.println("[SVN] LineColor : " + e.getMessage());
                        }
                    }
                }
            }
        }
        System.out.println("[SVN] Couleur #" + color + " appliquée sur " + changed + " arc(s).");
    }

    // -------------------------------------------------------------------------
    // 3. Code couleur automatique des stakeholders
    // -------------------------------------------------------------------------

    /**
     * Colore les nœuds «stakeholder» par tertile selon leur importanceScore :
     *   1er tertile → rouge  "FF4444"
     *   2e  tertile → orange "FFA500"
     *   3e  tertile → jaune  "FFFF00"
     */
    public void colorizeStakeholdersByRank(IRPModelElement root) {
        System.out.println("[SVN] Colorisation des stakeholders pour : " + root.getName());

        List<ScoredStakeholder> scored = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPClass && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_STAKEHOLDER)) {
                double score = readImportanceScore((IRPClass) el);
                scored.add(new ScoredStakeholder((IRPClass) el, score));
            }
        }

        if (scored.isEmpty()) {
            System.out.println("[SVN] Aucun stakeholder trouvé. Lancez d'abord SVN Calculate.");
            return;
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        int total   = scored.size();
        int cut1    = (int) Math.ceil(total / 3.0);
        int cut2    = (int) Math.ceil(2.0 * total / 3.0);

        List<IRPObjectModelDiagram> diagrams = findSVNDiagrams(root);

        for (int i = 0; i < total; i++) {
            String color = (i < cut1) ? "FF4444" : (i < cut2) ? "FFA500" : "FFFF00";
            applyFillColor(scored.get(i).element, color, diagrams);
        }
        System.out.println("[SVN] Colorisation terminée pour " + total + " stakeholder(s).");
    }

    private double readImportanceScore(IRPClass stakeholder) {
        IRPTag tag = stakeholder.getTag(SVNConstants.TAG_IMPORTANCE_SCORE);
        if (tag == null) return 0.0;
        try { return Double.parseDouble(tag.getValue()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private void applyFillColor(IRPModelElement element, String hexColor,
                                List<IRPObjectModelDiagram> diagrams) {
        for (IRPObjectModelDiagram diagram : diagrams) {
            IRPCollection graphElements = diagram.getGraphicalElements();
            for (int i = 1; i <= graphElements.getCount(); i++) {
                Object item = graphElements.getItem(i);
                if (item instanceof IRPGraphElement) {
                    IRPGraphElement ge = (IRPGraphElement) item;
                    if (element.equals(ge.getModelObject())) {
                        try {
                            ge.setGraphicalProperty("FillColor", hexColor);
                        } catch (Exception e) {
                            System.err.println("[SVN] FillColor " + element.getName() + " : " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 4. Création d'arc programmatique
    // -------------------------------------------------------------------------

    /**
     * Crée un «valuearc» entre les deux premiers nœuds SVN sélectionnés dans le
     * diagramme actif, puis ajoute sa représentation graphique.
     */
    public void createArcBetweenSelected(IRPApplication app, IRPProject project) {
        IRPCollection selected = app.getSelectedGraphElements();
        if (selected == null || selected.getCount() < 2) {
            System.err.println("[SVN] Sélectionnez 2 éléments (stakeholder ou system) dans le diagramme.");
            return;
        }

        IRPGraphNode srcNode = null, trgNode = null;
        IRPClassifier srcModel = null, trgModel = null;

        for (int i = 1; i <= selected.getCount() && trgNode == null; i++) {
            Object item = selected.getItem(i);
            if (!(item instanceof IRPGraphNode)) continue;
            IRPGraphNode node = (IRPGraphNode) item;
            IRPModelElement model = node.getModelObject();
            if (!(model instanceof IRPClassifier)) continue;
            boolean isSVN = RhapsodyWrapper.hasStereotype(model, SVNConstants.STEREOTYPE_STAKEHOLDER)
                    || RhapsodyWrapper.hasStereotype(model, SVNConstants.STEREOTYPE_SYSTEM);
            if (!isSVN) continue;
            if (srcNode == null) { srcNode = node; srcModel = (IRPClassifier) model; }
            else                 { trgNode = node; trgModel = (IRPClassifier) model; }
        }

        if (srcNode == null || trgNode == null) {
            System.err.println("[SVN] Impossible de trouver deux nœuds SVN valides.");
            return;
        }

        IRPDiagram diagram = srcNode.getDiagram();
        if (diagram == null) {
            System.err.println("[SVN] Aucun diagramme associé au nœud source.");
            return;
        }

        // Crée la relation modèle — addRelationTo(IRPClassifier, role1, type1, mult1, role2, type2, mult2, linkName)
        IRPRelation arc;
        try {
            arc = srcModel.addRelationTo(trgModel, "", "Association", "1", "", "Association", "1", "");
        } catch (Exception e) {
            System.err.println("[SVN] Échec création relation : " + e.getMessage());
            return;
        }

        // Applique le stéréotype via addStereotype(name, metaType)
        try {
            arc.addStereotype(SVNConstants.STEREOTYPE_VALUE_ARC, SVNConstants.METACLASS_ASSOCIATION);
        } catch (Exception e) {
            System.err.println("[SVN] Stéréotype valuearc : " + e.getMessage());
        }

        // Ajoute la représentation graphique
        try {
            int xSrc = getNodeCenterX(srcNode), ySrc = getNodeCenterY(srcNode);
            int xTrg = getNodeCenterX(trgNode), yTrg = getNodeCenterY(trgNode);
            diagram.addNewEdgeForElement(arc, srcNode, xSrc, ySrc, trgNode, xTrg, yTrg);
            project.save();
            System.out.println("[SVN] Arc créé entre '" + srcModel.getName() + "' et '" + trgModel.getName() + "'.");
        } catch (Exception e) {
            System.err.println("[SVN] Ajout graphique de l'arc : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers privés
    // -------------------------------------------------------------------------

    /** Retourne tous les IRPRelation «valuearc» sous un élément racine. */
    private List<IRPRelation> findValueArcs(IRPModelElement root) {
        List<IRPRelation> result = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPRelation
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_VALUE_ARC)) {
                result.add((IRPRelation) el);
            }
        }
        return result;
    }

    /**
     * Cherche tous les IRPObjectModelDiagram sous root qui portent le stéréotype SVNDiagram.
     * Utilise getNestedElementsByMetaClass("ObjectModelDiagram", 1) — disponible sur IRPModelElement.
     */
    private List<IRPObjectModelDiagram> findSVNDiagrams(IRPModelElement root) {
        List<IRPObjectModelDiagram> result = new ArrayList<>();
        // Remonte à la racine du projet si possible
        IRPModelElement current = root;
        while (current.getOwner() != null && !(current instanceof IRPProject)) {
            current = current.getOwner();
        }
        try {
            IRPCollection diags = current.getNestedElementsByMetaClass("ObjectModelDiagram", 1);
            for (int i = 1; i <= diags.getCount(); i++) {
                Object item = diags.getItem(i);
                if (item instanceof IRPObjectModelDiagram) {
                    result.add((IRPObjectModelDiagram) item);
                }
            }
        } catch (Exception e) {
            System.err.println("[SVN] findSVNDiagrams : " + e.getMessage());
        }
        return result;
    }

    private String getTagValue(IRPRelation arc, String tagName, String defaultValue) {
        try {
            IRPTag tag = arc.getTag(tagName);
            if (tag == null) return defaultValue;
            String val = tag.getValue();
            return (val == null || val.isEmpty()) ? defaultValue : val;
        } catch (Exception e) { return defaultValue; }
    }

    private int getNodeCenterX(IRPGraphNode node) {
        try {
            int x = Integer.parseInt(node.getGraphicalProperty("Left").getValue());
            int w = Integer.parseInt(node.getGraphicalProperty("Width").getValue());
            return x + w / 2;
        } catch (Exception e) { return 100; }
    }

    private int getNodeCenterY(IRPGraphNode node) {
        try {
            int y = Integer.parseInt(node.getGraphicalProperty("Top").getValue());
            int h = Integer.parseInt(node.getGraphicalProperty("Height").getValue());
            return y + h / 2;
        } catch (Exception e) { return 100; }
    }

    private static class ScoredStakeholder {
        final IRPClass element;
        final double score;
        ScoredStakeholder(IRPClass el, double s) { this.element = el; this.score = s; }
    }
}