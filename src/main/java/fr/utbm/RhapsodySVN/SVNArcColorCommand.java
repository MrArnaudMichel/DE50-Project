package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.DiagramService;

import javax.swing.*;
import java.awt.*;


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

        Color chosenColor = JColorChooser.showDialog(
                null,
                "Couleur du valuearc — " + selected.getName(),
                Color.BLUE
        );

        if (chosenColor == null) {
            System.out.println("[SVN] Sélection de couleur annulée.");
            return;
        }

        String hex = String.format("%02X%02X%02X",
                chosenColor.getRed(),
                chosenColor.getGreen(),
                chosenColor.getBlue());

        new DiagramService().setArcColor(selected, hex);
    }
}
