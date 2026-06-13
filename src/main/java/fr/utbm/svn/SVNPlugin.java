package fr.utbm.svn;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.RPUserPlugin;
import fr.utbm.svn.controller.Listener;
import fr.utbm.svn.service.impl.CalculationService;

/**
 * Main Rhapsody plugin entry point for the SVN (Stakeholder Value Network) analysis.
 *
 * <p>This class extends {@link RPUserPlugin} and is instantiated by the Rhapsody plugin
 * framework. On initialisation it registers a {@link Listener} that reacts to model
 * changes and re-computes stakeholder importance scores automatically.</p>
 */
public class SVNPlugin extends RPUserPlugin {

    private final Logger logger = Logger.getInstance();

    /**
     * Called by Rhapsody when the plugin is loaded.
     *
     * <p>Retrieves the active project, activates element-change notifications, and
     * registers the {@link Listener} as an application listener.</p>
     *
     * @param irpApplication the Rhapsody application instance provided by the framework
     */
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

    /**
     * Called when a custom menu item created by this plugin is invoked.
     * Not used by this plugin.
     */
    @Override
    public void RhpPluginInvokeItem() {}

    /**
     * Called when a menu item is selected by the user.
     * Not used by this plugin.
     *
     * @param s the identifier of the selected menu item
     */
    @Override
    public void OnMenuItemSelect(String s) {}

    /**
     * Called when a trigger event is fired within Rhapsody.
     * Not used by this plugin.
     *
     * @param s the trigger identifier
     */
    @Override
    public void OnTrigger(String s) {}

    /**
     * Called by Rhapsody to ask the plugin to release its resources.
     *
     * @return {@code false} to indicate no veto on cleanup
     */
    @Override
    public boolean RhpPluginCleanup() {
        return false;
    }

    /**
     * Called by Rhapsody for final cleanup after {@link #RhpPluginCleanup()}.
     * Not used by this plugin.
     */
    @Override
    public void RhpPluginFinalCleanup() {}
}
