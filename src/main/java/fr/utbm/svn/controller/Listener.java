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

public class Listener extends RPApplicationListener {
    private final IRPApplication app;
    private final IRPProject project;
    private final Logger logger = Logger.getInstance();
    private final ICalculationService calculationService;

    public Listener(IRPApplication app, IRPProject project, ICalculationService calculationService){
        this.app = app;
        this.project = project;
        this.calculationService = calculationService;
    }

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

    @Override
    public boolean afterAddElement(IRPModelElement irpModelElement) {
        if (!ValueArc.isValueArc(irpModelElement)) {
            return false;
        }
        ValueArc arc = new ValueArc((IRPDependency) irpModelElement);;
        logger.log("New value arc detected");

        IRPDiagram diagram = getSVNDiagrams(irpModelElement);

        try {
            // Stop notifications to avoid initializations of onElementsChanged
            project.setNotifyPluginOnElementsChanged(0);
            RhapsodyElementUpdater.updateArcLabel(arc, project);
            calculationService.calculateImportance(this.project, diagram);

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
                    IRPDiagram diagram = this.getSVNDiagrams(element.getOwner());
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
                            calculationService.calculateImportance(this.project, diagram);

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
                IRPDiagram diagram = app.getDiagramOfSelectedElement();
                try {
                    project.setNotifyPluginOnElementsChanged(0);
                    calculationService.calculateImportance(this.project, diagram);

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
    public boolean onDiagramOpen(IRPDiagram irpDiagram) { return false; }

    @Override
    public boolean onDoubleClick(IRPModelElement irpModelElement) {
        return false;
    }

    @Override
    public boolean onFeaturesOpen(IRPModelElement irpModelElement) { return false; }
}