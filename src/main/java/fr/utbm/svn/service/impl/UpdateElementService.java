package fr.utbm.svn.service.impl;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.Logger;
import fr.utbm.svn.constants.SVNConstants;
import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;

import static fr.utbm.svn.rhapsody.RhapsodyWrapper.setOrCreateTag;

public class UpdateElementService {

    public static void updateArcLabel(ValueArc arc, IRPProject project) {
        final Logger logger = Logger.getInstance();
        double score = arc.getScore();
        String label = String.format("%.2f", score);

        try {
            arc.setDisplayName(label);
            arc.setIsShowDisplayName(1);
            logger.log("Label arc '" + arc.getName() + "' → " + label);
            return;
        } catch (Exception e) {
            logger.error("setDisplayName failed : " + e.getMessage());
        }

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
                        logger.error("Label arc via GraphElement → " + label);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Graphic update failed : " + e.getMessage());
        }
    }

    public static void updateSystemTags(SVNSystem system, double totalLoopScore) {
        setOrCreateTag(system.getSystem(), "totalLoopScore", String.format("%.4f", totalLoopScore));
    }

    public static void updateStakeholderImportance(Stakeholder sh, double score) {
        IRPTag tag = sh.getTag(SVNConstants.TAG_IMPORTANCE_SCORE);
        if (tag == null) {
            try { tag = (IRPTag) sh.addNewAggr("Tag", SVNConstants.TAG_IMPORTANCE_SCORE); }
            catch (Exception ignored) {}
        }

        if (tag != null) {
            tag.setValue(String.format("%.4f", score));

            String currentName = sh.getName();
            String baseName = currentName;

            if (currentName.contains(" : ")) {
                baseName = currentName.split(" : ")[0].trim();
            } else if (currentName.contains(" _ ")) {
                baseName = currentName.split(" _ ")[0].trim();
            }

            sh.setDisplayName(baseName + " : " + String.format("%.4f", score));
        }
    }
}
