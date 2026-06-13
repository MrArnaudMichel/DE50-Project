package fr.utbm.svn.controller;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.Logger;
import fr.utbm.svn.constants.SVNConstants;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.rhapsody.RhapsodyWrapper;
import fr.utbm.svn.service.ICalculationService;
import fr.utbm.svn.rhapsody.RhapsodyElementUpdater;

import java.util.ArrayList;
import java.util.List;

/**
 * Rhapsody application listener that reacts to model-change events and triggers
 * stakeholder importance recalculation.
 *
 * <p>Listens for:
 * <ul>
 *   <li>New value arc additions ({@link #afterAddElement})</li>
 *   <li>Tag modifications on value arcs ({@link #onElementsChanged})</li>
 *   <li>Element deletions (detected through null GUID resolution in {@link #onElementsChanged})</li>
 * </ul>
 *
 * <p>Recalculations are debounced by 300 ms to avoid redundant computation when
 * several changes arrive in quick succession.</p>
 */
public class Listener extends RPApplicationListener {

    private final IRPApplication app;
    private final IRPProject project;
    private final Logger logger = Logger.getInstance();
    private final ICalculationService calculationService;

    /** Guards against re-entrant calls to {@link #onElementsChanged}. */
    private volatile boolean isProcessing = false;

    private final java.util.concurrent.ScheduledExecutorService scheduler =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

    /** Holds the last scheduled (possibly not yet executed) recalculation task. */
    private java.util.concurrent.ScheduledFuture<?> pendingRecalc;

    /**
     * Constructs a listener for the given Rhapsody context.
     *
     * @param app                the active Rhapsody application
     * @param project            the active Rhapsody project
     * @param calculationService the service used to compute stakeholder importance
     */
    public Listener(IRPApplication app, IRPProject project, ICalculationService calculationService) {
        this.app = app;
        this.project = project;
        this.calculationService = calculationService;
    }

    /**
     * Searches all SVN diagrams in the project and returns the one that contains
     * a graphical representation of the given model element.
     *
     * @param element the model element to locate
     * @return the SVN diagram containing {@code element}, or {@code null} if not found
     */
    private IRPDiagram getSVNDiagrams(IRPModelElement element) {
        IRPDiagram diagram = null;
        List<IRPDiagram> svnDiagrams = new ArrayList<>();
        IRPCollection allDiags = project.getNestedElementsByMetaClass("ObjectModelDiagram", 1);

        for (int i = 1; i <= allDiags.getCount(); i++) {
            Object item = allDiags.getItem(i);
            if (item instanceof IRPDiagram) {
                IRPModelElement diagElement = (IRPModelElement) item;
                if (RhapsodyWrapper.hasStereotype(diagElement, SVNConstants.STEREOTYPE_DIAGRAM)) {
                    svnDiagrams.add((IRPDiagram) diagElement);
                }
            }
        }

        for (IRPDiagram diag : svnDiagrams) {
            IRPCollection graphElems = diag.getGraphicalElements();

            for (int j = 1; j <= graphElems.getCount(); j++) {
                Object ge = graphElems.getItem(j);
                if (!(ge instanceof IRPGraphElement)) continue;

                IRPGraphElement graphElem = (IRPGraphElement) ge;

                if (element.equals(graphElem.getModelObject())) {
                    diagram = diag;
                }
            }
        }

        return diagram;
    }

    /**
     * Schedules a debounced recalculation of stakeholder importance.
     *
     * <p>Any previously pending task is cancelled before scheduling a new one with a
     * 300 ms delay. Notifications are temporarily disabled during the calculation to
     * prevent re-entrant listener calls.</p>
     *
     * @param diagram the SVN diagram on which the calculation is performed
     */
    private void scheduleRecalculation(IRPDiagram diagram) {
        if (pendingRecalc != null && !pendingRecalc.isDone()) {
            pendingRecalc.cancel(false);
        }
        pendingRecalc = scheduler.schedule(() -> {
            try {
                project.setNotifyPluginOnElementsChanged(0);
                calculationService.calculateImportance(this.project, diagram);
            } catch (Exception e) {
                logger.error("Debounced recalculation failed: " + e.getMessage());
            } finally {
                project.setNotifyPluginOnElementsChanged(1);
            }
        }, 300, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Called by Rhapsody after a new element is added to the model.
     *
     * <p>If the new element is a value arc, initialises its default tags,
     * updates its display label, and schedules a recalculation.</p>
     *
     * @param irpModelElement the newly added model element
     * @return {@code false} (no veto)
     */
    @Override
    public boolean afterAddElement(IRPModelElement irpModelElement) {
        if (!ValueArc.isValueArc(irpModelElement)) {
            return false;
        }
        ValueArc arc = new ValueArc((IRPDependency) irpModelElement);
        logger.log("New value arc detected");

        IRPDiagram diagram = getSVNDiagrams(irpModelElement);

        try {
            // Stop notifications to avoid initializations of onElementsChanged
            project.setNotifyPluginOnElementsChanged(0);
            RhapsodyElementUpdater.updateArcLabel(arc, project);
            this.scheduleRecalculation(diagram);

        } catch (Exception e) {
            logger.error("Error in afterAddElement: " + e.getMessage());
        } finally {
            project.setNotifyPluginOnElementsChanged(1);
        }

        return false;
    }

    /**
     * Called by Rhapsody when one or more model elements have been changed.
     *
     * <p>Iterates over the supplied GUIDs and, for each changed element:
     * <ul>
     *   <li>Treats a {@code null} lookup as a deletion and schedules recalculation.</li>
     *   <li>For {@link IRPTag} elements, checks whether a relevant arc tag
     *       ({@code benefitRanking} or {@code supplyImportance}) was modified and,
     *       if so, updates the arc label and schedules recalculation.</li>
     * </ul>
     *
     * <p>Uses {@link #isProcessing} to prevent re-entrant invocations caused by
     * tag updates performed during the calculation itself.</p>
     *
     * @param GUIDs comma-separated list of GUIDs of the changed elements
     * @return {@code false} (no veto)
     */
    @Override
    public boolean onElementsChanged(String GUIDs) {
        if (isProcessing) {
            logger.log("Already processing, skipping re-entrant call.");
            return false;
        }
        isProcessing = true;
        if (GUIDs == null || GUIDs.trim().isEmpty()) return false;

        String[] guidArray = GUIDs.split(",");
        boolean elementHasBeenDeleted = false;

        try {
            for (String guid : guidArray) {
                guid = guid.trim();
                if (guid.isEmpty()) continue;

                IRPModelElement element = project.findElementByGUID(guid);

                if (element == null) {
                    elementHasBeenDeleted = true;
                    continue;
                }

                if (element instanceof IRPTag) {
                    String tagName = element.getName();
                    IRPModelElement owner = element.getOwner();
                    if (owner == null) {
                        elementHasBeenDeleted = true;
                        continue;
                    }
                    IRPDiagram diagram = this.getSVNDiagrams(owner);
                    if (!SVNConstants.TAG_BENEFIT_RANKING.equals(tagName)
                            && !SVNConstants.TAG_SUPPLY_IMPORTANCE.equals(tagName)) {
                        continue;
                    }
                    if (ValueArc.isValueArc(owner)) {
                        logger.log("Tag '" + tagName + "' modified on arc: " + owner.getName());
                        try {
                            // Silence notifications from calculations modifications
                            project.setNotifyPluginOnElementsChanged(0);

                            RhapsodyElementUpdater.updateArcLabel(new ValueArc((IRPDependency) owner), project);
                            this.scheduleRecalculation(diagram);

                        } finally {
                            project.setNotifyPluginOnElementsChanged(1);
                        }

                        // An element justified the change, so this was not a deletion notification
                        elementHasBeenDeleted = false;
                        break;
                    }
                }
            }

            if (elementHasBeenDeleted) {
                IRPDiagram diagram = app.getDiagramOfSelectedElement();
                try {
                    project.setNotifyPluginOnElementsChanged(0);
                    this.scheduleRecalculation(diagram);
                } finally {
                    project.setNotifyPluginOnElementsChanged(1);
                }
            }

        } catch (Exception e) {
            logger.error("onElementsChanged : " + e.getMessage());
        } finally {
            isProcessing = false;
        }

        return false;
    }

    /**
     * Called by Rhapsody after the project has been closed.
     *
     * @param s the project name
     * @return {@code false} (no veto)
     */
    @Override
    public boolean afterProjectClose(String s) {
        return false;
    }

    /**
     * Called by Rhapsody before the project is closed.
     *
     * @param irpProject the project about to be closed
     * @return {@code false} (no veto)
     */
    @Override
    public boolean beforeProjectClose(IRPProject irpProject) {
        return false;
    }

    /**
     * Returns the unique identifier of this listener, used by Rhapsody to register it.
     *
     * @return the listener ID {@code "SVNListener"}
     */
    @Override
    public String getId() {
        return "SVNListener";
    }

    /**
     * Called when a diagram is opened.
     *
     * @param irpDiagram the opened diagram
     * @return {@code false} (no veto)
     */
    @Override
    public boolean onDiagramOpen(IRPDiagram irpDiagram) { return false; }

    /**
     * Called when an element is double-clicked in a diagram.
     *
     * @param irpModelElement the double-clicked element
     * @return {@code false} (no veto)
     */
    @Override
    public boolean onDoubleClick(IRPModelElement irpModelElement) {
        return false;
    }

    /**
     * Called when the features dialog of an element is opened.
     *
     * @param irpModelElement the element whose features dialog was opened
     * @return {@code false} (no veto)
     */
    @Override
    public boolean onFeaturesOpen(IRPModelElement irpModelElement) { return false; }
}
