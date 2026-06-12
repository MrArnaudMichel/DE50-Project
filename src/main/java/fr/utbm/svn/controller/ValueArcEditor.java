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
     * Result of the edit dialog.
     */
    public static final class Result {
        public final String benefitRanking;
        public final String supplyImportance;

        public Result(String benefitRanking, String supplyImportance) {
            this.benefitRanking = benefitRanking;
            this.supplyImportance = supplyImportance;
        }
    }

    /**
     * Shows a modal dialog with two dropdowns, pre-selected to the arc's
     * current tag values, allowing the user to pick new values for
     * BenefitRanking and SupplyImportance.
     *
     * @param arc the value arc whose current tag values should be used to
     *            pre-populate the dropdowns
     * @return the selected values, or {@code null} if the user cancelled
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

        // Build our own JOptionPane + JDialog instead of using the static
        // JOptionPane.showXxxDialog(null, ...) helpers. With a null owner,
        // the implicit shared frame Java creates has no real presence in
        // the native window manager, so when Rhapsody's main window
        // regains focus immediately after the double-click, the dialog can
        // be pushed behind it or torn down before it's painted.
        //
        // Using an explicit, always-on-top JDialog and forcing it to the
        // front after becoming visible avoids that.
        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
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

        if (value == null || !(value instanceof Integer) || (Integer) value != JOptionPane.OK_OPTION) {
            return null;
        }

        String selectedBenefit = (String) benefitCombo.getSelectedItem();
        String selectedSupply = (String) supplyCombo.getSelectedItem();
        return new Result(selectedBenefit, selectedSupply);
    }
}