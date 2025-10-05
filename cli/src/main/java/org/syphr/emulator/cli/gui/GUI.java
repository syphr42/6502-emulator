package org.syphr.emulator.cli.gui;

import org.syphr.emulator.cli.demo.Programs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GUI
{
    private final JFrame frame;
    private final AddressTableModel addressData;

    public GUI()
    {
        frame = new JFrame("6502 Emulator");
        frame.setPreferredSize(new Dimension(640, 480));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        var menuBar = new JMenuBar();
        var fileMenu = new JMenu("File");
        fileMenu.add(new AbstractAction("Exit")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.dispose();
            }
        });
        menuBar.add(fileMenu);
        var addressingMenu = new JMenu("Addressing");
        addressingMenu.add(new JMenuItem("Load ROM"));
        addressingMenu.add(new AbstractAction("Load Demo Program")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addressData.loadMemoryMap(Programs.simpleLoopWithSubRoutine());
            }
        });
        menuBar.add(addressingMenu);
        var cpuMenu = new JMenu("CPU");
        cpuMenu.add(new JMenuItem("Start"));
        menuBar.add(cpuMenu);
        frame.setJMenuBar(menuBar);

        addressData = new AddressTableModel();
        var cpuMon = new CPUMonitor(addressData);
        frame.getContentPane().add(cpuMon.getRoot());
    }

    public void show()
    {
        frame.pack();
        frame.setVisible(true);
    }
}
