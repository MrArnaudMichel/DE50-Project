package fr.utbm.svn;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPDiagram;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.RPUserPlugin;
import fr.utbm.svn.model.Listener;

public class SVNPlugin extends RPUserPlugin {

    private IRPApplication app;
    private IRPProject project;
    private IRPDiagram diagram;
    private Listener listener;

    private final Logger logger = Logger.getInstance();

    @Override
    public void RhpPluginInit(IRPApplication irpApplication) {
        this.app = irpApplication;
        this.project = app.activeProject();

        if (project == null) {
            this.logger.log("Project is null");
            return;
        }
        project.setNotifyPluginOnElementsChanged(1);

        this.diagram = this.app.getDiagramOfSelectedElement();

        this.listener = new Listener(this.app, this.project);
        // listener.connect(app);

        this.logger.log("Plugin init success");
    }

    @Override
    public void RhpPluginInvokeItem() {

    }

    @Override
    public void OnMenuItemSelect(String s) {

    }

    @Override
    public void OnTrigger(String s) {

    }

    @Override
    public boolean RhpPluginCleanup() {
        return false;
    }

    @Override
    public void RhpPluginFinalCleanup() {

    }
}
