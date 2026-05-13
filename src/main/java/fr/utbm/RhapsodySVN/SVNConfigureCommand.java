package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.ProfileService;

public class SVNConfigureCommand {
    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject project = app.activeProject();
        if (project != null) {
            new ProfileService(project).configureProfile(false);
        }
    }
}
