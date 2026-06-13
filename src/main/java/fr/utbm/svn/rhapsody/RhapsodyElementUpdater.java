package fr.utbm.svn.rhapsody;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.Logger;
import fr.utbm.svn.constants.SVNConstants;
import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.model.ValueLoop;

import java.util.List;

import static fr.utbm.svn.rhapsody.RhapsodyWrapper.setOrCreateTag;

public class RhapsodyElementUpdater {

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
                        logger.log("Label arc via GraphElement → " + label);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Graphic update failed : " + e.getMessage());
        }
    }

    public static void updateSystemTags(SVNSystem system, List<ValueLoop> loops, double totalLoopScore, List<ValueArc> allArcs) {
        setOrCreateTag(system.getSystem(), "totalLoopScore", String.format("%.4f", totalLoopScore));

        int i = 1;
        while (true) {
            try {
                IRPTag oldTag = system.getSystem().getTag("Loop_" + i);
                if (oldTag != null) {
                    oldTag.deleteFromProject();
                    i++;
                } else {
                    break;
                }
            } catch (Exception e) { break; }
        }

        try {
            IRPTag loopDetails = system.getSystem().getTag("loopDetails");
            if (loopDetails != null) loopDetails.deleteFromProject();
        } catch (Exception ignored) {}

        if (loops == null || loops.isEmpty()) {
            try {
                IRPTag mostImportant = system.getSystem().getTag("mostImportantVL");
                if (mostImportant != null) mostImportant.deleteFromProject();
            } catch (Exception ignored) {}
            for (ValueArc arc : allArcs) {
                fr.utbm.svn.rhapsody.RhapsodyWrapper.removeStereotype(arc.getDependency(), "bestValueArc");
            }
            return;
        }

        loops.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        ValueLoop bestLoop = loops.get(0);
        
        for (int j = 0; j < loops.size(); j++) {
            ValueLoop loop = loops.get(j);
            String tagName = "Loop_" + (j + 1);
            String value = String.format("%.4f", loop.getScore()) + " : " + loop.getNodes().values();
            setOrCreateTag(system.getSystem(), tagName, value);
            
            if (j == 0) {
                setOrCreateTag(system.getSystem(), "mostImportantVL", tagName);
            }
        }

        List<ValueArc> bestArcs = bestLoop.getArcs();
        for (ValueArc arc : allArcs) {
            boolean isBest = false;
            for (ValueArc bestArc : bestArcs) {
                if (arc.getGUID().equals(bestArc.getGUID())) {
                    isBest = true;
                    break;
                }
            }
            
            if (isBest) {
                fr.utbm.svn.rhapsody.RhapsodyWrapper.addStereotype(arc.getDependency(), "bestValueArc");
            } else {
                fr.utbm.svn.rhapsody.RhapsodyWrapper.removeStereotype(arc.getDependency(), "bestValueArc");
            }
        }
    }

    public static void updateStakeholderImportance(Stakeholder sh, double score) {
        final Logger logger = Logger.getInstance();
        try {
            setOrCreateTag(sh.getActor(), SVNConstants.TAG_IMPORTANCE_SCORE,
                    String.format("%.4f", score));

            String currentName = sh.getName();
            String baseName = currentName;
            if (currentName.contains(" : "))      baseName = currentName.split(" : ")[0].trim();
            else if (currentName.contains(" _ ")) baseName = currentName.split(" _ ")[0].trim();

            sh.setDisplayName(baseName + " : " + String.format("%.4f", score));
        } catch (Exception e) {
            logger.error("updateStakeholderImportance (score="
                    + String.format("%.4f", score) + ") : " + e.getMessage());
        }
    }
}