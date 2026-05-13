package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.CalculationService;
import fr.utbm.RhapsodySVN.service.ProfileService;

public class Main {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject project = app.activeProject();
        
        if (project == null) {
            System.err.println("[SVN] Aucun projet actif.");
            return;
        }

        boolean clean = false;
        for (String arg : args) {
            if ("-clean".equalsIgnoreCase(arg) || "-debug".equalsIgnoreCase(arg)) {
                clean = true;
                break;
            }
        }

        ProfileService profileService = new ProfileService(project);
        profileService.configureProfile(clean);
    }
}