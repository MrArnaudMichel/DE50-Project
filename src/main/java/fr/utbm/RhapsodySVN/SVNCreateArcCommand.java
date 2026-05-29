package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.DiagramService;

/**
 * Commande : crée un «valuearc» entre les deux éléments SVN sélectionnés
 * dans le diagramme actif, sans passer par la palette.
 *
 * Mode opératoire :
 *   1. Sélectionner deux éléments (stakeholder ou system) dans le diagramme SVN
 *   2. Lancer cette commande via Tools → SVN → Create Arc
 *
 * La relation est ajoutée au modèle et sa représentation graphique est dessinée
 * dans le diagramme courant.
 *
 * Déclenchement depuis Rhapsody : Tools → SVN → Create Arc
 */
public class SVNCreateArcCommand {

    public static void run(IRPApplication app) {
        IRPProject project = app.activeProject();
        if (project == null) {
            System.err.println("[SVN] SVNCreateArcCommand : aucun projet actif.");
            return;
        }
        new DiagramService().createArcBetweenSelected(app, project);
    }
}
