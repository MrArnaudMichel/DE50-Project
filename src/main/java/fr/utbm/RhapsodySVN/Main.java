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

        // --- Analyse des arguments CLI ---
        boolean clean      = false;
        boolean calculate  = false;
        boolean labelArcs  = false;
        boolean colorize   = false;

        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "-clean":     clean     = true; break;
                case "-calculate": calculate = true; break;
                case "-labels":    labelArcs = true; break;
                case "-colorize":  colorize  = true; break;
                // -debug conservé comme alias de -clean (comportement d'origine)
                case "-debug":     clean     = true; break;
            }
        }

     //   SVNConfigureCommand.run(app);
        SVNCalculateCommand.run(app);
        SVNEditArcCommand.run(app);
        SVNLabelArcCommand.run(project);
        SVNArcColorCommand.run(app);
        SVNColorizeStakeholdersCommand.run(app);
        SVNCreateArcCommand.run(app);
     //   SVNCleanCommand.run(app);


    }
}