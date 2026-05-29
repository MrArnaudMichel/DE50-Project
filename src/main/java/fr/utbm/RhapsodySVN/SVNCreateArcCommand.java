package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.DiagramService;

public class SVNCreateArcCommand {

    public static void run(IRPApplication app) {
        IRPProject project = app.activeProject();
        if (project == null) {
            System.err.println("[SVN] SVNCreateArcCommand : aucun projet actif.");
            return;
        }
        new DiagramService().createArcBetweenSelected(project);
    }
}