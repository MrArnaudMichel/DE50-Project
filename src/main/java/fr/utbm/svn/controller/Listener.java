package fr.utbm.svn.controller;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.Logger;
import fr.utbm.svn.constants.SVNConstants;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.service.ICalculationService;
import fr.utbm.svn.rhapsody.RhapsodyElementUpdater;

public class Listener extends RPApplicationListener {
    private final IRPApplication app;
    private final IRPProject project;
    private final Logger logger = Logger.getInstance();
    private final ICalculationService calculationService;
    private IRPDiagram diagram;

    public Listener(IRPApplication app, IRPProject project, ICalculationService calculationService){
        this.app = app;
        this.project = project;
        this.calculationService = calculationService;
        this.diagram = app.getDiagramOfSelectedElement();
    }

    @Override
    public boolean afterAddElement(IRPModelElement irpModelElement) {
        if (!ValueArc.isValueArc(irpModelElement)) {
            return false;
        }
        ValueArc arc = new ValueArc((IRPDependency) irpModelElement);;
        logger.log("New value arc detected");


        try {
            // Stop notifications to avoid initializations of onElementsChanged
            project.setNotifyPluginOnElementsChanged(0);
            RhapsodyElementUpdater.updateArcLabel(arc, project);
            calculationService.calculateImportance(this.diagram);

        } catch (Exception e) {
            logger.error("Error in afterAddElement: " + e.getMessage());
        } finally {
            project.setNotifyPluginOnElementsChanged(1);
        }

        return false;
    }

    @Override
    public boolean onElementsChanged(String GUIDs) {
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
                            calculationService.calculateImportance(this.diagram);

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
                    calculationService.calculateImportance(this.diagram);

                } finally {
                    project.setNotifyPluginOnElementsChanged(1);
                }
            }

        } catch (Exception e) {
            logger.error("Error on onElementsChanged : " + e.getMessage());
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
    public boolean onDiagramOpen(IRPDiagram irpDiagram) {
        this.diagram = irpDiagram;
        return false;
    }

    @Override
    public boolean onDoubleClick(IRPModelElement irpModelElement) {
        return false;
    }

    @Override
    public boolean onFeaturesOpen(IRPModelElement irpModelElement) {
        if (diagram == null) this.diagram = app.getDiagramOfSelectedElement();
        return false;
    }
}
