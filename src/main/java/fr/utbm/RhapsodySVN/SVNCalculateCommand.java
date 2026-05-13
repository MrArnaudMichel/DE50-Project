package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.CalculationService;

public class SVNCalculateCommand {
    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPModelElement selected = app.getSelectedElement();
        if (selected != null) {
            new CalculationService().calculateImportance(selected);
        }
    }
}
