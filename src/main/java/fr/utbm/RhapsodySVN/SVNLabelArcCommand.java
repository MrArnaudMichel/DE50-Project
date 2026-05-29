package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.DiagramService;

/**
 * Commande : met à jour les labels graphiques de tous les «valuearc»
 * sous l'élément sélectionné avec le format "B:<benefitRanking> | S:<supplyImportance>".
 *
 * Déclenchement depuis Rhapsody : Tools → SVN → Update Arc Labels
 * (ou via le mécanisme OnMenuItemSelect du plugin RPUserPlugin)
 */
public class SVNLabelArcCommand {

    public static void run(IRPApplication app) {
        IRPModelElement selected = app.getSelectedElement();
        if (selected == null) {
            System.err.println("[SVN] SVNLabelArcCommand : aucun élément sélectionné.");
            return;
        }
        new DiagramService().updateArcLabels(selected);
    }
}
