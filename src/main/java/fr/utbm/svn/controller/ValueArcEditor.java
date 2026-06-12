package fr.utbm.svn.controller;

import fr.utbm.svn.constants.SVNConstants;
import fr.utbm.svn.model.ValueArc;

import javax.swing.*;
import java.awt.*;

/**
 * Pure-Swing UI helper for editing the BenefitRanking and SupplyImportance
 * tags of a value arc.
 * <p>
 * This class performs NO Rhapsody API calls. It only builds and shows a
 * modal dialog and returns the selected values. All IRP* access must be
 * done by the caller (under the appropriate lock / notification handling).
 */
public final class ValueArcEditor {

    private ValueArcEditor() {}

    /**
     * Outcome of the edit dialog.
     */
    public enum Choice {
        /** User picked OK with the dropdown selections. */
        APPLY,
        /** User cancelled - nothing should change. */
        CANCEL,
        /** User asked to open the standard Rhapsody Features dialog instead. */
        OPEN_FEATURES
    }

    /**
     * Result of the edit dialog.
     */
    public static final class Result {
        public final Choice choice;
        public final String benefitRanking;
        public final String supplyImportance;

        private Result(Choice choice, String benefitRanking, String supplyImportance) {
            this.choice = choice;
            this.benefitRanking = benefitRanking;
            this.supplyImportance = supplyImportance;
        }

        static Result apply(String benefitRanking, String supplyImportance) {
            return new Result(Choice.APPLY, benefitRanking, supplyImportance);
        }

        static Result cancel() {
            return new Result(Choice.CANCEL, null, null);
        }

        static Result openFeatures() {
            return new Result(Choice.OPEN_FEATURES, null, null);
        }
    }

    /**
     * Shows a modal dialog with two dropdowns, pre-selected to the arc's
     * current tag values, allowing the user to pick new values for
     * BenefitRanking and SupplyImportance, cancel, or fall back to the
     * standard Rhapsody Features dialog for this element.
     *
     * @param arc the value arc whose current tag values should be used to
     *            pre-populate the dropdowns
     * @return the user's choice, including selected values when applicable
     */
    public static Result showEditDialog(ValueArc arc) {
        String currentBenefit = arc.getTagValue(SVNConstants.TAG_BENEFIT_RANKING, SVNConstants.LITERALS_BENEFIT[0]);
        String currentSupply = arc.getTagValue(SVNConstants.TAG_SUPPLY_IMPORTANCE, SVNConstants.LITERALS_SUPPLY[0]);

        JComboBox<String> benefitCombo = new JComboBox<>(SVNConstants.LITERALS_BENEFIT);
        benefitCombo.setSelectedItem(currentBenefit);

        JComboBox<String> supplyCombo = new JComboBox<>(SVNConstants.LITERALS_SUPPLY);
        supplyCombo.setSelectedItem(currentSupply);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Benefit Ranking:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(benefitCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Supply Importance:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(supplyCombo, gbc);

        String title = "Edit Value Arc";
        try {
            String arcName = arc.getName();
            if (arcName != null && !arcName.isEmpty()) {
                title = "Edit Value Arc - " + arcName;
            }
        } catch (Exception ignored) {
            // keep default title
        }

        // Three explicit buttons: OK, Cancel, and an escape hatch to the
        // standard Rhapsody Features dialog for users who need to edit
        // something this popup doesn't cover (description, other tags...).
        String okOption = "OK";
        String cancelOption = "Cancel";
        String featuresOption = "Open Features...";
        Object[] options = {okOption, cancelOption, featuresOption};

        // Build our own JOptionPane + JDialog instead of using the static
        // JOptionPane.showXxxDialog(null, ...) helpers. With a null owner,
        // the implicit shared frame Java creates has no real presence in
        // the native window manager, so when Rhapsody's main window
        // regains focus immediately after the double-click, the dialog can
        // be pushed behind it or torn down before it's painted.
        //
        // Using an explicit, always-on-top JDialog and forcing it to the
        // front after becoming visible avoids that.
        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_CANCEL_OPTION,
                null,
                options,
                okOption
        );
        JDialog dialog = optionPane.createDialog(title);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(null);

        // setVisible(true) on an APPLICATION_MODAL dialog blocks until it
        // is disposed, so toFront()/requestFocus() must run once the dialog
        // is actually opened, not after - hence the WindowListener.
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                dialog.toFront();
                dialog.requestFocus();
            }
        });

        dialog.setVisible(true);

        Object value = optionPane.getValue();
        dialog.dispose();

        if (okOption.equals(value)) {
            String selectedBenefit = (String) benefitCombo.getSelectedItem();
            String selectedSupply = (String) supplyCombo.getSelectedItem();
            return Result.apply(selectedBenefit, selectedSupply);
        }

        if (featuresOption.equals(value)) {
            return Result.openFeatures();
        }

        // Cancel, window closed (X button / Esc), or any other value.
        return Result.cancel();
    }
}