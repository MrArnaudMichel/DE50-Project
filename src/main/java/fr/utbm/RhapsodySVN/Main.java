package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.CalculationService;
import fr.utbm.RhapsodySVN.service.DiagramService;
import fr.utbm.RhapsodySVN.service.ProfileService;

public class Main {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject project = app.activeProject();

        if (project == null) {
            System.err.println("[SVN] Aucun projet actif.");
            return;
        }


        SVNConfigureCommand.run(app);
        SVNCalculateCommand.run(app);
        SVNEditArcCommand.run(app);
       // SVNLabelArcCommand.run(project);
      //  SVNCleanCommand.run(app);


    }
}