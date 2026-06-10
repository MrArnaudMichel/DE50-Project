package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.UpdateElementService;
import fr.utbm.svn.Logger;
import fr.utbm.svn.service.ICalculationService;
import fr.utbm.svn.service.impl.CalculationService;

public class Listener extends RPApplicationListener {
    private final IRPApplication app;
    private final IRPProject project;
    private final Logger logger = Logger.getInstance();
    private final ICalculationService calculationService;

    public Listener(IRPApplication app, IRPProject project){
        this.app = app;
        this.project = project;
        this.calculationService = new CalculationService();
        this.connect(app);
    }

    @Override
    public boolean afterAddElement(IRPModelElement irpModelElement) {
        if (!ValueArc.isValueArc(irpModelElement)) {
            return false;
        }
        ValueArc arc = (ValueArc) irpModelElement;
        logger.log("New value arc detected");


        try {
            // Stop notifications to avoid initializations of onElementsChanged
            project.setNotifyPluginOnElementsChanged(0);
            UpdateElementService.updateArcLabel(arc, project);
            calculationService.calculateImportance(project, app.getDiagramOfSelectedElement());

        } catch (Exception e) {
            logger.error("Error in afterAddElement: " + e.getMessage());
        } finally {
            project.setNotifyPluginOnElementsChanged(1);
        }

        return false;
    }

    @Override
    public boolean onElementsChanged(String s) {
        // TODO
        return super.onElementsChanged(s);
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
        return false;
    }

    @Override
    public boolean onDoubleClick(IRPModelElement irpModelElement) {
        return false;
    }

    @Override
    public boolean onFeaturesOpen(IRPModelElement irpModelElement) {
        return false;
    }
}
