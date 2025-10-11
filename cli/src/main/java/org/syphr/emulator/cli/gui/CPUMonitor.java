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

    public CPUMonitor(AddressTableModel addressData, OpLogTableModel opLogData, CycleLogTableModel cycleLogData)
    {
        root = new JPanel();
        root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        var mainSplit = new JSplitPane();
        mainSplit.setLeftComponent(createAddressTableScrollPane(addressData));
        mainSplit.setRightComponent(createLogSplitPane(opLogData, cycleLogData));
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

    private static JSplitPane createLogSplitPane(OpLogTableModel opLogData, CycleLogTableModel cycleLogData)
    {
        var logSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        logSplit.setTopComponent(createOpLogTableScrollPane(opLogData));
        logSplit.setBottomComponent(createCycleLogTableScrollPane(cycleLogData));

        return logSplit;
    }

    private static JScrollPane createOpLogTableScrollPane(OpLogTableModel model)
    {
        var table = new JTable(model);

        var scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        setAutoScroll(scrollPane);

        return scrollPane;
    }

    private static JScrollPane createCycleLogTableScrollPane(CycleLogTableModel model)
    {
        var table = new JTable(model);
        table.getColumn(CycleLogTableModel.Column.CLOCK_CYCLE.getDisplayName()).setPreferredWidth(300);
        table.getColumn(CycleLogTableModel.Column.PROGRAM_COUNTER.getDisplayName()).setPreferredWidth(250);
        table.getColumn(CycleLogTableModel.Column.STATUS.getDisplayName()).setPreferredWidth(400);
        table.getColumn(CycleLogTableModel.Column.ACCUMULATOR.getDisplayName()).setPreferredWidth(250);
        table.getColumn(CycleLogTableModel.Column.X.getDisplayName()).setPreferredWidth(250);
        table.getColumn(CycleLogTableModel.Column.Y.getDisplayName()).setPreferredWidth(250);
        table.getColumn(CycleLogTableModel.Column.STACK_POINTER.getDisplayName()).setPreferredWidth(250);

        var scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        setAutoScroll(scrollPane);

        return scrollPane;
    }

    private static void setAutoScroll(JScrollPane scrollPane)
    {
        scrollPane.getVerticalScrollBar()
                  .addAdjustmentListener(event -> event.getAdjustable().setValue(event.getAdjustable().getMaximum()));
    }
}
