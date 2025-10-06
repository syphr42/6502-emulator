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

import lombok.Getter;
import org.syphr.emulator.common.Value;
import org.syphr.emulator.cpu.Address;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class CPUMonitor
{
    @Getter
    private final JPanel root;

    public CPUMonitor(AddressTableModel addressData, OpLogTableModel opLogData)
    {
        root = new JPanel();
        root.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        JSplitPane mainSplit = new JSplitPane();
        root.add(mainSplit, gbc);

        JScrollPane addressScroll = new JScrollPane();
        mainSplit.setLeftComponent(addressScroll);
        JTable addressTable = new JTable();
        addressScroll.setViewportView(addressTable);
        addressTable.setDefaultRenderer(Address.class, new DefaultTableCellRenderer()
        {
            @Override
            protected void setValue(Object value)
            {
                setText("%04X".formatted(((Address) value).data()));
            }
        });
        addressTable.setDefaultRenderer(Value.class, new DefaultTableCellRenderer()
        {
            @Override
            protected void setValue(Object value)
            {
                setText("%02X".formatted(((Value) value).data()));
            }
        });
        addressTable.setModel(addressData);

        JScrollPane opLogScroll = new JScrollPane();
        mainSplit.setRightComponent(opLogScroll);
        JTable opLogTable = new JTable();
        opLogScroll.setViewportView(opLogTable);
        opLogTable.setModel(opLogData);
    }
}
