package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.UpdateElementService;
import fr.utbm.svn.Logger;

public class Listener extends RPApplicationListener {
    private IRPProject project;

    public Listener(IRPProject project){
        this.project = project;
    }

    @Override
    public boolean afterAddElement(IRPModelElement irpModelElement) {
        if (!ValueArc.isValueArc(irpModelElement)) {
            return false;
        }
        ValueArc arc = (ValueArc) irpModelElement;
        Logger.log("New value arc detected");


        try {
            // Stop notifications to avoid initializations of onElementsChanged
            project.setNotifyPluginOnElementsChanged(0);

            arc.initDefaultTags();
            UpdateElementService.updateArcLabel(arc, project);
            calculationService.calculateImportance(project, app.getDiagramOfSelectedElement());

        } catch (Exception e) {
            Logger.error("Erreur lors de l'afterAddElement : " + e.getMessage());
        } finally {
            project.setNotifyPluginOnElementsChanged(1);
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
