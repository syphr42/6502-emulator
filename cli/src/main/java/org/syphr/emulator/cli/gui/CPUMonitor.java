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
import org.syphr.emulator.cli.gui.OpLogTableModel.Column;
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        JSplitPane mainSplit = new JSplitPane();
        mainSplit.setLeftComponent(createAddressTableScrollPane(addressData));
        mainSplit.setRightComponent(createOpLogTableScrollPane(opLogData));
        root.add(mainSplit, gbc);
    }

    private static JScrollPane createAddressTableScrollPane(AddressTableModel addressData)
    {
        JTable addressTable = new JTable();
        addressTable.setModel(addressData);
        addressTable.getColumnModel().getColumn(0).setPreferredWidth(200);
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

        JScrollPane addressScroll = new JScrollPane();
        addressScroll.setViewportView(addressTable);

        return addressScroll;
    }

    private static JScrollPane createOpLogTableScrollPane(OpLogTableModel opLogData)
    {
        JTable opLogTable = new JTable();
        opLogTable.setModel(opLogData);
        opLogTable.getColumn(Column.CLOCK_CYCLE.getDisplayName()).setPreferredWidth(300);
        opLogTable.getColumn(Column.PROGRAM_COUNTER.getDisplayName()).setPreferredWidth(250);
        opLogTable.getColumn(Column.OP.getDisplayName()).setPreferredWidth(600);
        opLogTable.getColumn(Column.STATUS.getDisplayName()).setPreferredWidth(400);
        opLogTable.getColumn(Column.ACCUMULATOR.getDisplayName()).setPreferredWidth(250);
        opLogTable.getColumn(Column.X.getDisplayName()).setPreferredWidth(250);
        opLogTable.getColumn(Column.Y.getDisplayName()).setPreferredWidth(250);
        opLogTable.getColumn(Column.STACK_POINTER.getDisplayName()).setPreferredWidth(250);

        JScrollPane opLogScroll = new JScrollPane();
        opLogScroll.setViewportView(opLogTable);

        return opLogScroll;
    }
}
