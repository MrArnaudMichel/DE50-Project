package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.ProfileService;

public class SVNCleanCommand {

    // Appelé par SVNPlugin.OnMenuItemSelect
    public static void run(IRPApplication app) {
        IRPProject project = app.activeProject();
        if (project != null) {
            System.out.println("[SVN] Lancement du nettoyage complet...");
            new ProfileService(project).configureProfile(true);
        }
    }

    // Entrée CLI existante — inchangée
    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }
}
