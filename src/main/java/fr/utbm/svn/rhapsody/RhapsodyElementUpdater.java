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

/**
 * Utility class that writes computation results back into the Rhapsody model.
 *
 * <p>All methods are stateless and {@code static}; this class is not meant to be
 * instantiated. It acts as a bridge between the domain model (scores, loops) and the
 * Rhapsody API (tags, display names, stereotypes).</p>
 */
public class RhapsodyElementUpdater {

    /**
     * Updates the display label of a value arc in the diagram with its computed score.
     *
     * <p>First attempts to set the label via {@link IRPDependency#setDisplayName}. If that
     * call fails (e.g. for read-only dependencies), falls back to setting the label through
     * the graphical element's text property by scanning all object-model diagrams.</p>
     *
     * @param arc     the value arc whose label should be updated
     * @param project the Rhapsody project used to iterate over diagrams in the fallback path
     */
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

    /**
     * Writes loop analysis results as tags on the system element and marks the arcs
     * belonging to the best loop with the {@code bestValueArc} stereotype.
     *
     * <p>Existing {@code Loop_N} and {@code loopDetails} tags are deleted first to avoid
     * stale data. Loops are sorted by descending score before being written so that
     * {@code Loop_1} always refers to the highest-scoring loop.</p>
     *
     * @param system         the SVN system element to update
     * @param loops          all detected value loops (may be {@code null} or empty)
     * @param totalLoopScore the sum of all loop scores
     * @param allArcs        all value arcs in the diagram, used to apply or remove the
     *                       {@code bestValueArc} stereotype
     */
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

    /**
     * Writes the importance score of a stakeholder as a tag and updates its display name
     * in the Rhapsody diagram.
     *
     * <p>The display name is formatted as {@code "<baseName> : <score>"}, where
     * {@code baseName} is derived by stripping any previously appended score suffix
     * (separated by {@code " : "} or {@code " _ "}).</p>
     *
     * @param sh    the stakeholder to update
     * @param score the computed importance score to store and display
     */
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
