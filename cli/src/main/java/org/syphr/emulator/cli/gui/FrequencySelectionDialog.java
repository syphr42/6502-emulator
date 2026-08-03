/*
 * Copyright © 2025-2026 Gregory P. Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.syphr.emulator.cli.gui;

import org.syphr.emulator.cli.clock.ClockPeriod;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class FrequencySelectionDialog extends JDialog
{
    private final JTextField numberField = new JTextField(4);
    private final JComboBox<String> unitCombo = new JComboBox<>(new String[] {"hz", "khz", "mhz"});
    private final JLabel validationLabel = new JLabel();
    private final JButton okButton = new JButton("OK");

    public FrequencySelectionDialog(Frame owner, ClockPeriod initial, Consumer<ClockPeriod> consumer)
    {
        super(owner, "Set Clock Frequency", true);
        setLayout(new BorderLayout(10, 10));

        // Input panel: integer field + unit combo
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Clock Frequency"));
        inputPanel.add(new JLabel("Value:"));
        numberField.setHorizontalAlignment(SwingConstants.RIGHT);
        inputPanel.add(numberField);
        inputPanel.add(unitCombo);

        // TODO align validation text to the left
        // Validation label setup (reserve space)
        validationLabel.setFont(validationLabel.getFont().deriveFont(Font.PLAIN, 10f));
        validationLabel.setForeground(Color.RED);
        validationLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        validationLabel.setText(" ");
        Dimension valPref = new Dimension(200, validationLabel.getPreferredSize().height);
        validationLabel.setMinimumSize(valPref);
        validationLabel.setPreferredSize(valPref);
        validationLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, valPref.height));

        JPanel fieldWithValidation = new JPanel();
        fieldWithValidation.setLayout(new BoxLayout(fieldWithValidation, BoxLayout.Y_AXIS));
        fieldWithValidation.add(inputPanel);
        fieldWithValidation.add(validationLabel);

        add(fieldWithValidation, BorderLayout.CENTER);

        // TODO add separator above buttons

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton.setEnabled(false);
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener((ActionEvent e) -> {
            String val = numberField.getText().trim();
            String unit = (String) unitCombo.getSelectedItem();
            try {
                ClockPeriod cp = ClockPeriod.of(val + unit);
                consumer.accept(cp);
                dispose();
            } catch (IllegalArgumentException ex) {
                validationLabel.setText("Invalid frequency.");
            }
        });

        cancelButton.addActionListener((ActionEvent _) -> dispose());

        DocumentListener dl = new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                validateField();
            }

            public void removeUpdate(DocumentEvent e)
            {
                validateField();
            }

            public void changedUpdate(DocumentEvent e)
            {
                validateField();
            }
        };

        numberField.getDocument().addDocumentListener(dl);
        unitCombo.addActionListener(_ -> validateField());

        initializeFrom(initial);

        pack();
        setLocationRelativeTo(owner);

        // Clear the placeholder text after packing
        validationLabel.setText("");
    }

    private void initializeFrom(ClockPeriod cp)
    {
        long nanos = cp.duration().toNanos();
        String[] units = new String[] {"mhz", "khz", "hz"};
        long[] base = new long[] {ClockPeriod.ONE_MHZ.toNanos(),
                                  ClockPeriod.ONE_KHZ.toNanos(),
                                  ClockPeriod.ONE_HZ.toNanos()};

        int chosenVal = -1;
        String chosenUnit = "hz";

        for (int i = 0; i < units.length; i++) {
            long mult = base[i] / nanos; // floor division
            if (mult >= 1) {
                if (mult > 999) {
                    mult = 999; // clamp to allowed max
                }
                chosenVal = (int) mult;
                chosenUnit = units[i];
                break;
            }
        }

        if (chosenVal == -1) {
            // duration too long -> use 1hz as fallback
            chosenVal = 1;
            chosenUnit = "hz";
        }

        numberField.setText(String.valueOf(chosenVal));
        unitCombo.setSelectedItem(chosenUnit);
    }

    private void validateField()
    {
        String num = numberField.getText().trim();
        String unit = (String) unitCombo.getSelectedItem();
        String err = "";
        if (num.isEmpty()) {
            err = "Value is required (1–999).";
        } else {
            try {
                int v = Integer.parseInt(num);
                if (v < 1 || v > 999) {
                    err = "Value must be between 1 and 999.";
                } else {
                    try {
                        ClockPeriod.of(v + unit);
                    } catch (IllegalArgumentException e) {
                        err = "Invalid frequency. Use 1hz–999mhz.";
                    }
                }
            } catch (NumberFormatException e) {
                err = "Value must be a number (1–999).";
            }
        }
        validationLabel.setText(err);
        okButton.setEnabled(err.isEmpty());
    }
}
