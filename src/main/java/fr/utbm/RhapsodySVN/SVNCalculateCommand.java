package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.CalculationService;

public class SVNCalculateCommand {

    public static void run(IRPApplication app) {
        IRPProject project = app.activeProject();
        if (project == null) {
            System.err.println("[SVN] Aucun projet actif.");
            return;
        }

        new CalculationService().calculateImportance(project, app.getDiagramOfSelectedElement());
    }


    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }
}