package fr.utbm.svn;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.RhapsodyAppServer;
import fr.utbm.RhapsodySVN.SVNPlugin;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Boolean isDebug = Boolean.parseBoolean(args[0]);
        Logger logger = Logger.getInstance();
        logger.setDebug(isDebug);

        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject project = app.activeProject();

        if (project == null) {
            logger.log("No active project.");
            return;
        }

        fr.utbm.RhapsodySVN.SVNPlugin plugin = new SVNPlugin();
        plugin.RhpPluginInit(app);

        // to make sure that the client keeps listening Rhapsody,
        // without terminating, use a blocking I/O operation.
        try {
            System.in.read();
        } catch (IOException e) {
            plugin.RhpPluginCleanup();
            plugin.RhpPluginFinalCleanup();
            logger.error(e.getMessage());
        }
    }
}
