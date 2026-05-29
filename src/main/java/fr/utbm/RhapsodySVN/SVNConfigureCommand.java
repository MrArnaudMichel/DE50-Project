package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.ProfileService;

public class SVNConfigureCommand {

    // Appelé par SVNPlugin.OnMenuItemSelect
    public static void run(IRPApplication app) {
        IRPProject project = app.activeProject();
        if (project != null) {
            new ProfileService(project).configureProfile(false);
        }
    }

    // Entrée CLI existante — inchangée
    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }
}
