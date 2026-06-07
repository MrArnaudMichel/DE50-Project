package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject project = app.activeProject();

        if (project == null) {
            System.err.println("[SVN] Aucun projet actif.");
            return;
        }

        SVNPlugin plugin = new SVNPlugin();
        plugin.RhpPluginInit(app);
        //plugin.RhpPluginInvokeItem();

        // to make sure that the client keeps listening Rhapsody,
        // without terminating, use a blocking I/O operation.
        try {
            System.in.read();
        } catch (IOException e) {
            plugin.RhpPluginCleanup();
            plugin.RhpPluginFinalCleanup();
            e.printStackTrace();
        }

    }
}