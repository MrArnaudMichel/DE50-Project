package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;

import javax.swing.*;

public class SVNEditArcCommand {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }


    public static void run(IRPApplication app) {
        IRPModelElement selected = app.getSelectedElement();

        if (!(selected instanceof IRPFlow)) {
            JOptionPane.showMessageDialog(null,
                    "Sélectionnez un arc «valuearc» dans le diagramme.",
                    "SVN Edit Arc", JOptionPane.WARNING_MESSAGE);
            return;
        }

        IRPFlow arc = (IRPFlow) selected;

        if (!RhapsodyWrapper.hasStereotype(arc, SVNConstants.STEREOTYPE_VALUE_ARC)) {
            JOptionPane.showMessageDialog(null,
                    "L'élément sélectionné n'est pas un «valuearc».",
                    "SVN Edit Arc", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Valeurs actuelles
        String currentBenefit = getTagValue(arc, SVNConstants.TAG_BENEFIT_RANKING);
        String currentSupply  = getTagValue(arc, SVNConstants.TAG_SUPPLY_IMPORTANCE);

        // Choix benefitRanking
        String[] benefitOptions = SVNConstants.LITERALS_BENEFIT;
        String chosenBenefit = (String) JOptionPane.showInputDialog(
                null,
                "Benefit Ranking pour '" + arc.getName() + "' :",
                "SVN Edit Arc — Benefit Ranking",
                JOptionPane.QUESTION_MESSAGE,
                null,
                benefitOptions,
                currentBenefit.isEmpty() ? benefitOptions[0] : currentBenefit
        );
        if (chosenBenefit == null) return; // annulé

        // Choix supplyImportance
        String[] supplyOptions = SVNConstants.LITERALS_SUPPLY;
        String chosenSupply = (String) JOptionPane.showInputDialog(
                null,
                "Supply Importance pour '" + arc.getName() + "' :",
                "SVN Edit Arc — Supply Importance",
                JOptionPane.QUESTION_MESSAGE,
                null,
                supplyOptions,
                currentSupply.isEmpty() ? supplyOptions[0] : currentSupply
        );
        if (chosenSupply == null) return; // annulé

        // Écriture des valeurs
        setTagValue(arc, SVNConstants.TAG_BENEFIT_RANKING, chosenBenefit);
        setTagValue(arc, SVNConstants.TAG_SUPPLY_IMPORTANCE, chosenSupply);

        app.activeProject().save();

        System.out.println("[SVN] Arc '" + arc.getName() + "' mis à jour :"
                + " benefitRanking=" + chosenBenefit
                + " supplyImportance=" + chosenSupply);
    }

    private static String getTagValue(IRPFlow arc, String tagName) {
        try {
            IRPTag tag = arc.getTag(tagName);
            if (tag == null) return "";
            String val = tag.getValue();
            return (val == null) ? "" : val;
        } catch (Exception e) {
            return "";
        }
    }

    private static void setTagValue(IRPFlow arc, String tagName, String value) {
        try {
            IRPTag tag = arc.getTag(tagName);
            if (tag == null) {
                tag = (IRPTag) arc.addNewAggr("Tag", tagName);
            }
            if (tag != null) {
                tag.setValue(value);
            }
        } catch (Exception e) {
            System.err.println("[SVN] Impossible d'écrire le tag " + tagName + " : " + e.getMessage());
        }
    }
}