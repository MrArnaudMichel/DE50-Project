package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.CalculationService;

public class SVNCalculateCommand {

    // Appelé par SVNPlugin.OnMenuItemSelect
    public static void run(IRPApplication app) {
        IRPModelElement selected = app.getSelectedElement();
        if (selected != null) {
            new CalculationService().calculateImportance(selected);
        }
    }

    // Entrée CLI existante — inchangée
    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }
}
