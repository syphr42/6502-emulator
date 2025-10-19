/*
 * Copyright © 2025 Gregory P. Moyer
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

import org.syphr.emulator.cli.memory.MemoryMap;
import org.syphr.emulator.cpu.Address;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ROMSelectionDialog extends JDialog
{
    private final JTextField filePathField = new JTextField(30);
    private final JLabel fileValidationLabel = new JLabel();
    private final JTextField addressField = new JTextField("8000", 6);
    private final JLabel addressValidationLabel = new JLabel();
    private final JButton okButton = new JButton("OK");

    private boolean fileValid = false;
    private boolean addressValid = false;

    public ROMSelectionDialog(Frame owner, Consumer<MemoryMap> memoryMapConsumer)
    {
        super(owner, "Select ROM", true);
        setLayout(new BorderLayout(10, 10));

        // File selection panel
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.setBorder(BorderFactory.createTitledBorder("ROM File"));
        filePanel.add(filePathField, BorderLayout.CENTER);
        JButton browseButton = new JButton("Browse...");
        filePanel.add(browseButton, BorderLayout.EAST);

        // Address panel
        JPanel addressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addressPanel.setBorder(BorderFactory.createTitledBorder("Start Address (hex, 16-bit)"));
        addressPanel.add(new JLabel("0x"));
        addressPanel.add(addressField);

        // Validation label for file
        fileValidationLabel.setFont(fileValidationLabel.getFont().deriveFont(Font.PLAIN, 10f));
        fileValidationLabel.setForeground(Color.RED);
        fileValidationLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        // Reserve vertical space for one line of text
        fileValidationLabel.setText(" "); // single space to ensure height
        Dimension fileValPref = fileValidationLabel.getPreferredSize();
        fileValidationLabel.setMinimumSize(fileValPref);
        fileValidationLabel.setPreferredSize(fileValPref);
        fileValidationLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fileValPref.height));

        // Validation label for address
        addressValidationLabel.setFont(addressValidationLabel.getFont().deriveFont(Font.PLAIN, 10f));
        addressValidationLabel.setForeground(Color.RED);
        addressValidationLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        // Reserve vertical space for one line of text
        addressValidationLabel.setText(" ");
        Dimension addrValPref = addressValidationLabel.getPreferredSize();
        addressValidationLabel.setMinimumSize(addrValPref);
        addressValidationLabel.setPreferredSize(addrValPref);
        addressValidationLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, addrValPref.height));

        // Panels for field + validation
        JPanel fileFieldPanel = new JPanel(new BorderLayout());
        fileFieldPanel.add(filePanel, BorderLayout.NORTH);
        fileFieldPanel.add(fileValidationLabel, BorderLayout.CENTER);

        JPanel addressFieldPanel = new JPanel(new BorderLayout());
        addressFieldPanel.add(addressPanel, BorderLayout.NORTH);
        addressFieldPanel.add(addressValidationLabel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);

        // Center panel contains both input areas stacked vertically and stays at the top
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(fileFieldPanel);
        centerPanel.add(addressFieldPanel);

        // Use a wrapper panel with BorderLayout to keep centerPanel at the top
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(centerPanel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        // Horizontal separator above buttons
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Panel for separator + buttons
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(separator);
        southPanel.add(buttonPanel);
        add(southPanel, BorderLayout.SOUTH);

        // Initial state
        okButton.setEnabled(false);
        okButton.addActionListener((_) -> {
            String filePath = filePathField.getText().trim();
            String addressHex = addressField.getText().trim();

            try {
                var memoryMap = MemoryMap.of(Address.ofHex(addressHex), Path.of(filePath));
                memoryMapConsumer.accept(memoryMap);
                dispose();
            } catch (IOException | RuntimeException e) {
                fileValidationLabel.setText("Error loading file: " + e.getMessage());
            }
        });

        // Listeners
        filePathField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                validateFileField();
            }

            public void removeUpdate(DocumentEvent e)
            {
                validateFileField();
            }

            public void changedUpdate(DocumentEvent e)
            {
                validateFileField();
            }
        });
        addressField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                validateAddressField();
            }

            public void removeUpdate(DocumentEvent e)
            {
                validateAddressField();
            }

            public void changedUpdate(DocumentEvent e)
            {
                validateAddressField();
            }
        });

        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Binary Files (*.bin)", "bin"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                filePathField.setText(selected.getAbsolutePath());
            }
        });

        cancelButton.addActionListener((ActionEvent e) -> dispose());

        pack();
        setLocationRelativeTo(owner);

        // Clear the placeholder text after packing
        fileValidationLabel.setText("");
        addressValidationLabel.setText("");
    }

    private void validateFileField()
    {
        String filePath = filePathField.getText().trim();
        String fileError = "";
        if (filePath.isEmpty()) {
            fileError = "File path is required.";
            fileValid = false;
        } else if (!new File(filePath).isFile()) {
            fileError = "File does not exist.";
            fileValid = false;
        } else {
            fileValid = true;
        }
        fileValidationLabel.setText(fileValid ? "" : fileError);
        // Also update OK button state
        okButton.setEnabled(fileValid && addressValid);
    }

    private void validateAddressField()
    {
        String address = addressField.getText().trim();
        String addressError = "";
        if (address.isEmpty()) {
            addressError = "Address is required.";
            addressValid = false;
        } else if (!address.matches("(?i)[0-9a-f]{1,4}")) {
            addressError = "Must be 1-4 hex digits (0-FFFF).";
            addressValid = false;
        } else {
            try {
                int value = Integer.parseInt(address, 16);
                if (value < 0x0000 || value > 0xFFFF) {
                    addressError = "Address out of range (0-FFFF).";
                    addressValid = false;
                } else {
                    addressValid = true;
                }
            } catch (NumberFormatException e) {
                addressError = "Invalid hex value.";
                addressValid = false;
            }
        }
        addressValidationLabel.setText(addressValid ? "" : addressError);
        // Also update OK button state
        okButton.setEnabled(fileValid && addressValid);
    }
}
