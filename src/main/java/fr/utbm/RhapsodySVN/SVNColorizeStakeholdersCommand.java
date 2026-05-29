package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.DiagramService;

public class SVNColorizeStakeholdersCommand {

    public static void run(IRPApplication app) {
        IRPModelElement selected = app.getSelectedElement();
        if (selected == null) {
            System.err.println("[SVN] SVNColorizeStakeholdersCommand : aucun élément sélectionné.");
            return;
        }
        new DiagramService().colorizeStakeholdersByRank(selected);
    }
}
