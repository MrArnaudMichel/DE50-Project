package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.DiagramService;

/**
 * Commande : colore automatiquement les nœuds «stakeholder» dans le diagramme
 * selon leur rang d'importance (calculé au préalable par SVNCalculateCommand).
 *
 * Code couleur par tertile (recommandation INCOSE 2018) :
 *   - 1er tertile (plus important) → rouge
 *   - 2e tertile                   → orange
 *   - 3e tertile (moins important) → jaune
 *
 * Déclenchement depuis Rhapsody : Tools → SVN → Colorize Stakeholders
 *
 * Prérequis : SVNCalculateCommand doit avoir été exécuté au préalable
 * (le tag "importanceScore" doit être renseigné sur chaque stakeholder).
 */
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
