package fr.utbm.RhapsodySVN.service;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;

import java.util.ArrayList;
import java.util.List;

import static fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper.getTagValue;

public class DiagramService {

    //Not showing yet
    public void updateArcLabels(IRPModelElement root) {
        System.out.println("[SVN] Mise à jour des labels d'arcs pour : " + root.getName());

        List<IRPDependency> arcs = findValueArcs(root);
        if (arcs.isEmpty()) {
            System.out.println("[SVN] Aucun valuearc trouvé.");
            return;
        }

        int updated = 0;
        for (IRPDependency arc : arcs) {
            String label = buildArcLabel(arc);
            System.out.println("[SVN] Arc '" + arc.getName() + "' label=[" + label + "]");

            try {
                arc.setDisplayName(label);
                arc.setIsShowDisplayName(1);
                updated++;
                System.out.println("[SVN]   → setDisplayName OK");
            } catch (Exception e) {
                System.err.println("[SVN]   → setDisplayName échoué : " + e.getMessage());

                List<IRPObjectModelDiagram> diagrams = findSVNDiagrams(root);
                for (IRPObjectModelDiagram diagram : diagrams) {
                    IRPCollection graphElements = diagram.getGraphicalElements();
                    for (int i = 1; i <= graphElements.getCount(); i++) {
                        Object item = graphElements.getItem(i);
                        if (item instanceof IRPGraphElement) {
                            IRPGraphElement ge = (IRPGraphElement) item;
                            if (arc.equals(ge.getModelObject())) {
                                try {
                                    ge.setGraphicalPropertyOfText("Keyword", "Text", label);
                                    updated++;
                                    System.out.println("[SVN]   → Keyword OK");
                                } catch (Exception e2) {
                                    System.err.println("[SVN]   → Keyword échoué : " + e2.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("[SVN] Labels mis à jour sur " + updated + " arc(s).");
    }

    private String buildArcLabel(IRPDependency arc) {
        String benefit = getTagValue(arc, SVNConstants.TAG_BENEFIT_RANKING, "?");
        String supply  = getTagValue(arc, SVNConstants.TAG_SUPPLY_IMPORTANCE, "?");
        return "B:" + benefit + " | S:" + supply;
    }

    public void setArcColor(IRPModelElement modelElement, String hexColor) {
        if (!(modelElement instanceof IRPDependency)) {
            System.err.println("[SVN] setArcColor : l'élément sélectionné n'est pas un Flow.");
            return;
        }
        IRPDependency arc = (IRPDependency) modelElement;
        if (!RhapsodyWrapper.hasStereotype(arc, SVNConstants.STEREOTYPE_VALUE_ARC)) {
            System.err.println("[SVN] setArcColor : le Flow n'est pas un «valuearc».");
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

    public void colorizeStakeholdersByRank(IRPModelElement root) {
        System.out.println("[SVN] Colorisation des stakeholders pour : " + root.getName());

        List<ScoredStakeholder> scored = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPActor
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_STAKEHOLDER)) {
                double score = readImportanceScore(el);
                scored.add(new ScoredStakeholder(el, score));
            }
        }

        if (scored.isEmpty()) {
            System.out.println("[SVN] Aucun stakeholder trouvé. Lancez d'abord SVN Calculate.");
            return;
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        int total = scored.size();
        int cut1  = (int) Math.ceil(total / 3.0);
        int cut2  = (int) Math.ceil(2.0 * total / 3.0);

        List<IRPObjectModelDiagram> diagrams = findSVNDiagrams(root);

        for (int i = 0; i < total; i++) {
            String color = (i < cut1) ? "FF4444" : (i < cut2) ? "FFA500" : "FFFF00";
            ScoredStakeholder ss = scored.get(i);
            applyFillColor(ss.element, color, diagrams);

            try {
                IRPTag tag = ss.element.getTag(SVNConstants.TAG_IMPORTANCE_SCORE);
                if (tag != null && tag.getValue() != null && !tag.getValue().isEmpty()) {
                    String score = String.format("%.4f", Double.parseDouble(tag.getValue()));
                    ss.element.setDisplayName(ss.element.getName() + "\n(" + score + ")");
                    ss.element.setIsShowDisplayName(1);
                }
            } catch (Exception e) {
                System.err.println("[SVN] setDisplayName stakeholder : " + e.getMessage());
            }
        }
        System.out.println("[SVN] Colorisation terminée pour " + total + " stakeholder(s).");
    }

    private double readImportanceScore(IRPModelElement stakeholder) {
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

    public void createArcBetweenSelected(IRPProject project) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPCollection selected = app.getSelectedGraphElements();
        if (selected == null || selected.getCount() < 2) {
            System.err.println("[SVN] Sélectionnez 2 éléments (stakeholder ou system) dans le diagramme.");
            return;
        }

        IRPGraphNode srcNode = null, trgNode = null;
        IRPModelElement srcModel = null, trgModel = null;

        for (int i = 1; i <= selected.getCount() && trgNode == null; i++) {
            Object item = selected.getItem(i);
            if (!(item instanceof IRPGraphNode)) continue;
            IRPGraphNode node = (IRPGraphNode) item;
            IRPModelElement model = node.getModelObject();
            if (model == null) continue;
            boolean isSVN = RhapsodyWrapper.hasStereotype(model, SVNConstants.STEREOTYPE_STAKEHOLDER)
                    || RhapsodyWrapper.hasStereotype(model, SVNConstants.STEREOTYPE_SYSTEM);
            if (!isSVN) continue;
            if (srcNode == null) { srcNode = node; srcModel = model; }
            else                 { trgNode = node; trgModel = model; }
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

        IRPModelElement owner = diagram.getOwner();

        IRPDependency arc;
        try {
            arc = (IRPDependency) owner.addNewAggr("Flow", "valuearc_" + srcModel.getName() + "_" + trgModel.getName());
        } catch (Exception e) {
            System.err.println("[SVN] Échec création Flow : " + e.getMessage());
            return;
        }

        try {
            arc.setDependent(srcModel);
            arc.setDependsOn(trgModel);
        } catch (Exception e) {
            System.err.println("[SVN] Échec setEnd1/setEnd2 : " + e.getMessage());
        }

        try {
            arc.addStereotype(SVNConstants.STEREOTYPE_VALUE_ARC, SVNConstants.METACLASS_ASSOCIATION);
        } catch (Exception e) {
            System.err.println("[SVN] Stéréotype valuearc : " + e.getMessage());
        }

        try {
            int xSrc = getNodeCenterX(srcNode), ySrc = getNodeCenterY(srcNode);
            int xTrg = getNodeCenterX(trgNode), yTrg = getNodeCenterY(trgNode);
            diagram.addNewEdgeForElement(arc, srcNode, xSrc, ySrc, trgNode, xTrg, yTrg);
            project.save();
            System.out.println("[SVN] Arc Flow créé entre '"
                    + srcModel.getName() + "' et '" + trgModel.getName() + "'.");
        } catch (Exception e) {
            System.err.println("[SVN] Ajout graphique de l'arc : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers privés
    // -------------------------------------------------------------------------

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

    private List<IRPObjectModelDiagram> findSVNDiagrams(IRPModelElement root) {
        List<IRPObjectModelDiagram> result = new ArrayList<>();
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
        final IRPModelElement element;
        final double score;
        ScoredStakeholder(IRPModelElement el, double s) { this.element = el; this.score = s; }
    }
}