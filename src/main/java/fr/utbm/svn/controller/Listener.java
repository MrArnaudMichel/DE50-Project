package fr.utbm.svn.controller;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.Logger;
import fr.utbm.svn.constants.SVNConstants;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.rhapsody.RhapsodyWrapper;
import fr.utbm.svn.service.ICalculationService;
import fr.utbm.svn.rhapsody.RhapsodyElementUpdater;

import javax.swing.SwingUtilities;
import java.util.concurrent.locks.ReentrantLock;

public class Listener extends RPApplicationListener {
    private final IRPApplication app;
    private final IRPProject project;
    private final Logger logger = Logger.getInstance();
    private final ICalculationService calculationService;

    /**
     * Guards every section of code that talks to the Rhapsody API
     * (IRPProject / IRPModelElement / IRPTag, etc.).
     * <p>
     * Rhapsody's COM/JNI bridge is not safe to call concurrently from
     * multiple threads. This lock ensures that:
     * <ul>
     *     <li>the listener's own change-handling (afterAddElement,
     *     onElementsChanged) never interleaves with itself, and</li>
     *     <li>the value-arc edit dialog (triggered from onDoubleClick,
     *     potentially on the Swing EDT) never writes to the model while
     *     the listener is mid-calculation, and vice versa.</li>
     * </ul>
     * Combined with {@code setNotifyPluginOnElementsChanged(0)} around
     * each guarded section, this also prevents reentrant notification
     * storms while a write is in progress.
     */
    static final ReentrantLock RHAPSODY_LOCK = new ReentrantLock();

    public Listener(IRPApplication app, IRPProject project, ICalculationService calculationService){
        this.app = app;
        this.project = project;
        this.calculationService = calculationService;
    }

    @Override
    public boolean afterAddElement(IRPModelElement irpModelElement) {
        if (!ValueArc.isValueArc(irpModelElement)) {
            return false;
        }
        ValueArc arc = new ValueArc((IRPDependency) irpModelElement);;
        logger.log("New value arc detected");

        RHAPSODY_LOCK.lock();
        try {
            // Stop notifications to avoid initializations of onElementsChanged
            project.setNotifyPluginOnElementsChanged(0);
            RhapsodyElementUpdater.updateArcLabel(arc, project);
            calculationService.calculateImportance(this.project, app);
          //  app.refreshAllViews();

        } catch (Exception e) {
            logger.error("Error in afterAddElement: " + e.getMessage());
        } finally {
            project.setNotifyPluginOnElementsChanged(1);
            RHAPSODY_LOCK.unlock();
        }

        return false;
    }

    @Override
    public boolean onElementsChanged(String GUIDs) {
        if (GUIDs == null || GUIDs.trim().isEmpty()) return false;

        String[] guidArray = GUIDs.split(",");
        boolean elementHasBeenDeleted = false;

        RHAPSODY_LOCK.lock();
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
                    if (!SVNConstants.TAG_BENEFIT_RANKING.equals(tagName)
                            && !SVNConstants.TAG_SUPPLY_IMPORTANCE.equals(tagName)) {
                        continue;
                    }
                    IRPModelElement owner = element.getOwner();
                    if (ValueArc.isValueArc(owner)) {
                        logger.log("Tag '" + tagName + "' modified on arc: " + owner.getName());
                        try {
                            // Silence notifications from calculations modifications
                            project.setNotifyPluginOnElementsChanged(0);

                            RhapsodyElementUpdater.updateArcLabel(new ValueArc((IRPDependency) owner), project);
                            calculationService.calculateImportance(this.project, app);
                          //  app.refreshAllViews();

                        } finally {
                            project.setNotifyPluginOnElementsChanged(1);
                        }

                        // An element justify a change so it was not a deletion notification
                        elementHasBeenDeleted = false;
                        break;
                    }
                }
            }

            if (elementHasBeenDeleted) {
                try {
                    project.setNotifyPluginOnElementsChanged(0);
                    calculationService.calculateImportance(this.project, app);
                   // app.refreshAllViews();

                } finally {
                    project.setNotifyPluginOnElementsChanged(1);
                }
            }

        } catch (Exception e) {
            logger.error("Error on onElementsChanged : " + e.getMessage());
        } finally {
            RHAPSODY_LOCK.unlock();
        }

        return false;
    }

    @Override
    public boolean afterProjectClose(String s) {
        return false;
    }

    @Override
    public boolean beforeProjectClose(IRPProject irpProject) {
        return false;
    }

    @Override
    public String getId() {
        return "SVNListener";
    }

    @Override
    public boolean onDiagramOpen(IRPDiagram irpDiagram) { return false; }

    /**
     * Double-clicking a value arc opens a small dialog with dropdowns for
     * BenefitRanking and SupplyImportance, instead of (or in addition to)
     * Rhapsody's default Features dialog.
     * <p>
     * If the element is not a value arc, default behavior is preserved by
     * returning {@code false}.
     */
    @Override
    public boolean onDoubleClick(IRPModelElement irpModelElement) {
        try {
            if (!ValueArc.isValueArc(irpModelElement)) {
                return false;
            }
            final ValueArc arc = new ValueArc((IRPDependency) irpModelElement);
            java.util.Timer timer = new java.util.Timer("SVNEditArcDialogTimer", true);
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> editValueArc(arc));
                }
            }, 150);
            return true;

        } catch (Throwable t) {
            logger.error("Error in onDoubleClick (value arc edit): " + t);
            return false;
        }
    }

    /**
     * Shows the edit dialog for the given value arc and, if confirmed,
     * writes the selected tag values back to the model. Must be called on
     * the Swing EDT, and is intended to run asynchronously, well after the
     * native onDoubleClick callback has already returned to Rhapsody.
     */
    private void editValueArc(ValueArc arc) {
        try {
            ValueArcEditor.Result result = ValueArcEditor.showEditDialog(arc);

            switch (result.choice) {
                case CANCEL:
                    return;

                case OPEN_FEATURES:
                    RHAPSODY_LOCK.lock();
                    try {
                        // Re-select the arc so the Features dialog opens for
                        // the right element, and bring Rhapsody's window to
                        // the front - opening a native dialog from this
                        // background/timer thread context can otherwise
                        // leave it behind the main window or unfocused.
                        reselectArc(arc);
                        arc.getDependency().openFeaturesDialog(1);
                       // app.bringWindowToTop();
                    } finally {
                        RHAPSODY_LOCK.unlock();
                    }
                    return;

                case APPLY:
                    RHAPSODY_LOCK.lock();
                    try {
                        project.setNotifyPluginOnElementsChanged(0);

                        // setOrCreateTag already handles the case where the tag is
                        // inherited from a (possibly read-only) profile: it creates a
                        // local override on the instance so the write always succeeds.
                        RhapsodyWrapper.setOrCreateTag(arc.getDependency(), SVNConstants.TAG_BENEFIT_RANKING, result.benefitRanking);
                        RhapsodyWrapper.setOrCreateTag(arc.getDependency(), SVNConstants.TAG_SUPPLY_IMPORTANCE, result.supplyImportance);

                        RhapsodyElementUpdater.updateArcLabel(arc, project);

                        // calculationService.calculateImportance relies on
                        // app.getDiagramOfSelectedElement(), which reflects
                        // Rhapsody's CURRENT selection. By the time this
                        // async block runs (after the modal dialog), the
                        // selection may have been lost/changed - re-select
                        // the arc first so the diagram resolves correctly.
                        reselectArc(arc);

                        calculationService.calculateImportance(this.project, app);
                      //  app.refreshAllViews();

                    } finally {
                        project.setNotifyPluginOnElementsChanged(1);
                        RHAPSODY_LOCK.unlock();
                    }
                    return;
            }
        } catch (Throwable t) {
            logger.error("Error while editing value arc: " + t);
        }
    }

    /**
     * Re-selects the given value arc's underlying model element in
     * Rhapsody, so that {@code IRPApplication.getSelectedElement()} and
     * {@code getDiagramOfSelectedElement()} resolve to this arc and its
     * containing diagram again.
     */
    private void reselectArc(ValueArc arc) {
        try {
            IRPCollection toSelect = app.createNewCollection();
            toSelect.addItem(arc.getDependency());
            app.selectModelElements(toSelect);
        } catch (Exception e) {
            logger.error("Could not re-select value arc before recalculation: " + e.getMessage());
        }
    }

    @Override
    public boolean onFeaturesOpen(IRPModelElement irpModelElement) { return false; }
}