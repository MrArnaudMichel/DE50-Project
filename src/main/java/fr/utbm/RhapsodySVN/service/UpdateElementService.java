package fr.utbm.RhapsodySVN.service;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.model.ValueLoop;

import java.util.List;

import static fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper.getTagValue;
import static fr.utbm.RhapsodySVN.service.CalculationService.getArcScore;

public final class UpdateElementService {


    public static void updateArcLabel(IRPDependency arc, IRPProject project) {
        String benefit = getTagValue(arc, SVNConstants.TAG_BENEFIT_RANKING, "MIGHT_BE");
        String supply  = getTagValue(arc, SVNConstants.TAG_SUPPLY_IMPORTANCE, "LOW");
        double score   = getArcScore(benefit, supply);
        String label   = String.format("%.2f", score);

        // Tentative 1 : setDisplayName sur l'élément modèle
        try {
            arc.setDisplayName(label);
            arc.setIsShowDisplayName(1);
            System.out.println("[SVN] Label arc '" + arc.getName() + "' → " + label);
            return;
        } catch (Exception e) {
            System.err.println("[SVN] setDisplayName échoué : " + e.getMessage());
        }

        // Tentative 2 : via les GraphElements du diagramme
        try {
            IRPCollection allDiags = project.getNestedElementsByMetaClass(
                    "ObjectModelDiagram", 1);
            for (int i = 1; i <= allDiags.getCount(); i++) {
                Object d = allDiags.getItem(i);
                if (!(d instanceof IRPObjectModelDiagram)) continue;
                IRPCollection graphElems =
                        ((IRPObjectModelDiagram) d).getGraphicalElements();
                for (int j = 1; j <= graphElems.getCount(); j++) {
                    Object ge = graphElems.getItem(j);
                    if (!(ge instanceof IRPGraphElement)) continue;
                    IRPGraphElement graphElem = (IRPGraphElement) ge;
                    if (arc.equals(graphElem.getModelObject())) {
                        graphElem.setGraphicalPropertyOfText("Keyword", "Text", label);
                        System.out.println("[SVN] Label arc via GraphElement → " + label);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SVN] Mise à jour graphique échouée : " + e.getMessage());
        }
    }

    public static void updateSystemTags(IRPModelElement system,
                                  List<ValueLoop> loops,
                                  double totalLoopScore) {
        setOrCreateTag(system, "totalLoopScore",
                String.format("%.4f", totalLoopScore));
        setOrCreateTag(system, "loopCount",
                String.valueOf(loops.size()));

        // Optionnel : détail des loops sous forme lisible
        StringBuilder detail = new StringBuilder();
        for (ValueLoop loop : loops) {
            detail.append(loop.getNodes().toString())
                    .append("=")
                    .append(String.format("%.4f", loop.getScore()))
                    .append("; ");
        }
        setOrCreateTag(system, "loopDetails", detail.toString());
    }

    private static void setOrCreateTag(IRPModelElement el, String tagName, String value) {
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
}
