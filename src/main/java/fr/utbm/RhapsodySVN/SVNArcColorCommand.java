package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.DiagramService;

import javax.swing.*;
import java.awt.*;

/**
 * Commande : ouvre un sélecteur de couleur Java et applique la couleur choisie
 * sur le «valuearc» sélectionné dans Rhapsody.
 *
 * Déclenchement depuis Rhapsody : Tools → SVN → Set Arc Color
 *
 * Note : JColorChooser est disponible dans le JRE embarqué par Rhapsody (Java 1.8+).
 * Si l'affichage pose problème en contexte headless, remplacer par une saisie
 * texte via JOptionPane ou passer la couleur en argument de ligne de commande.
 */
public class SVNArcColorCommand {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }


        public static void run(IRPApplication app) {
        IRPModelElement selected = app.getSelectedElement();
        if (selected == null) {
            System.err.println("[SVN] SVNArcColorCommand : aucun élément sélectionné.");
            return;
        }

        // Dialogue de sélection de couleur
        Color chosenColor = JColorChooser.showDialog(
                null,
                "Couleur du valuearc — " + selected.getName(),
                Color.BLUE
        );

        if (chosenColor == null) {
            System.out.println("[SVN] Sélection de couleur annulée.");
            return;
        }

        // Convertit en hex RGB sans alpha (format Rhapsody : "RRGGBB")
        String hex = String.format("%02X%02X%02X",
                chosenColor.getRed(),
                chosenColor.getGreen(),
                chosenColor.getBlue());

        new DiagramService().setArcColor(selected, hex);
    }
}
