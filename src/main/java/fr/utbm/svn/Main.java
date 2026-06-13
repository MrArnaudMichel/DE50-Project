package fr.utbm.svn;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

import java.io.IOException;

/**
 * Standalone entry point for running the SVN plugin outside of Rhapsody.
 *
 * <p>Connects to the currently active Rhapsody application, initialises the
 * {@link SVNPlugin}, and then blocks on {@code System.in} to keep the JVM alive
 * while Rhapsody fires change notifications.</p>
 */
public class Main {

    /**
     * Application entry point.
     *
     * <p>Accepts an optional {@code debug} argument (case-insensitive) to enable
     * verbose logging before connecting to Rhapsody.</p>
     *
     * @param args optional command-line arguments; pass {@code "debug"} to activate debug logging
     */
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
            logger.setDebug(true);
        }

        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject project = app.activeProject();

        if (project == null) {
            logger.log("No active project.");
            return;
        }

        SVNPlugin plugin = new SVNPlugin();
        plugin.RhpPluginInit(app);

        // Blocks the main thread so the process keeps receiving Rhapsody change events.
        try {
            System.in.read();
        } catch (IOException e) {
            plugin.RhpPluginCleanup();
            plugin.RhpPluginFinalCleanup();
            logger.error(e.getMessage());
        }
    }
}
