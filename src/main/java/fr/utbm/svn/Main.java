package fr.utbm.svn;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

/**
 * Standalone entry point for running the SVN plugin outside of Rhapsody.
 *
 * <p>Connects to the currently active Rhapsody application, initialises the
 * {@link SVNPlugin}, and then blocks on {@code System.in} to keep the JVM alive
 * while Rhapsody fires change notifications.</p>
 *
 * <p>A JVM shutdown hook ensures {@link SVNPlugin#RhpPluginCleanup()} and
 * {@link SVNPlugin#RhpPluginFinalCleanup()} are always called, whether the
 * process exits normally (Enter key) or is interrupted (Ctrl+C).</p>
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
        if (args.length > 0 && "debug".equalsIgnoreCase(args[0])) {
            logger.setDebug(true);
        }

        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        if (app == null) {
            logger.error("No active Rhapsody application found. Is Rhapsody running?");
            return;
        }

        IRPProject project = app.activeProject();
        if (project == null) {
            logger.error("No active project found in Rhapsody.");
            return;
        }

        SVNPlugin plugin = new SVNPlugin();
        plugin.RhpPluginInit(app);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log("Shutdown — cleaning up plugin.");
            plugin.RhpPluginCleanup();
            plugin.RhpPluginFinalCleanup();
        }));

        logger.log("SVN plugin running. Press Enter to stop.");
        try {
            System.in.read();
        } catch (Exception e) {
            logger.error("Unexpected error while waiting: " + e.getMessage());
        }

        System.exit(0);
    }
}
