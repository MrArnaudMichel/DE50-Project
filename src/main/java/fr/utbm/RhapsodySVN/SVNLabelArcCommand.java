package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.DiagramService;

public class SVNLabelArcCommand {

    public static void run(IRPProject project) {
        new DiagramService().updateArcLabels(project);
    }
    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject project = app.activeProject();
        run(project);
    }
}