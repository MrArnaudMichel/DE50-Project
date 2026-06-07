package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;

import javax.swing.*;

import java.awt.*;

import static fr.utbm.RhapsodySVN.service.CalculationService.getArcScore;

public class SVNEditArcCommand {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }


    public static void run(IRPApplication app) {
        IRPModelElement selected = app.getSelectedElement();

        if (!(selected instanceof IRPDependency)) {
            JOptionPane.showMessageDialog(null,
                    "Sélectionnez un arc «valuearc» dans le diagramme.",
                    "SVN Edit Arc", JOptionPane.WARNING_MESSAGE);
            return;
        }

        IRPDependency arc = (IRPDependency) selected;

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

        updateArcLabel(arc, chosenBenefit, chosenSupply);

        app.activeProject().save();

        System.out.println("[SVN] Arc '" + arc.getName() + "' mis à jour :"
                + " benefitRanking=" + chosenBenefit
                + " supplyImportance=" + chosenSupply);
    }

    private static String getTagValue(IRPDependency arc, String tagName) {
        try {
            IRPTag tag = arc.getTag(tagName);
            if (tag == null) return "";
            String val = tag.getValue();
            return (val == null) ? "" : val;
        } catch (Exception e) {
            return "";
        }
    }

    private static void setTagValue(IRPDependency arc, String tagName, String value) {
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

    /**
     * Met à jour le label affiché sur l'arc directement après modification des tags.
     * Calcule le score via la même matrice que CalculationService.
     */
    private static void updateArcLabel(IRPDependency arc, String benefit, String supply) {
        double score = getArcScore(benefit, supply);
        // Format du label : score calculé + les deux tags pour lisibilité
        String label = String.format("%.2f", score);

        // Tentative 1 : setDisplayName sur l'élément modèle
        try {
            arc.setDisplayName(label);
            arc.setIsShowDisplayName(1);
            System.out.println("[SVN] Label arc mis à jour via setDisplayName : " + label);
            return;
        } catch (Exception e) {
            System.err.println("[SVN] setDisplayName échoué : " + e.getMessage());
        }

        // Tentative 2 : via les éléments graphiques du diagramme
        try {
            IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
            IRPProject project = app.activeProject();
            IRPCollection allDiags = project.getNestedElementsByMetaClass("ObjectModelDiagram", 1);
            for (int i = 1; i <= allDiags.getCount(); i++) {
                Object item = allDiags.getItem(i);
                if (!(item instanceof IRPObjectModelDiagram)) continue;
                IRPObjectModelDiagram diagram = (IRPObjectModelDiagram) item;
                IRPCollection graphElements = diagram.getGraphicalElements();
                for (int j = 1; j <= graphElements.getCount(); j++) {
                    Object ge = graphElements.getItem(j);
                    if (!(ge instanceof IRPGraphElement)) continue;
                    IRPGraphElement graphElem = (IRPGraphElement) ge;
                    if (arc.equals(graphElem.getModelObject())) {
                        graphElem.setGraphicalPropertyOfText("Keyword", "Text", label);
                        System.out.println("[SVN] Label arc mis à jour via GraphElement : " + label);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SVN] Mise à jour graphique du label échouée : " + e.getMessage());
        }
    }
}