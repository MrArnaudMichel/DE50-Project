package fr.utbm.svn;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.RPUserPlugin;
import fr.utbm.svn.controller.Listener;
import fr.utbm.svn.service.impl.CalculationService;

public class SVNPlugin extends RPUserPlugin {

    private final Logger logger = Logger.getInstance();

    @Override
    public void RhpPluginInit(IRPApplication irpApplication) {
        IRPProject project = irpApplication.activeProject();

        if (project == null) {
            this.logger.log("Project is null");
            return;
        }
        project.setNotifyPluginOnElementsChanged(1);

        Listener listener = new Listener(irpApplication, project, new CalculationService());
        listener.connect(irpApplication);

        this.logger.log("Plugin init success");
    }

    @Override
    public void RhpPluginInvokeItem() {}

    @Override
    public void OnMenuItemSelect(String s) {}

    @Override
    public void OnTrigger(String s) {}

    @Override
    public boolean RhpPluginCleanup() {
        return false;
    }

    @Override
    public void RhpPluginFinalCleanup() {}
}