package org.syphr.emulator.cli.gui;

import lombok.Getter;
import org.syphr.emulator.common.Value;
import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.CPUState;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class CPUMonitor
{
    @Getter
    private final JPanel root;

    public CPUMonitor(AddressTableModel addressData)
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
        var opLogData = new OpLogTableModel();
        opLogTable.setModel(opLogData);
    }

    public void updateState(CPUState state)
    {
        // TODO
    }
}
