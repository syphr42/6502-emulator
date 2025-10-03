package org.syphr.emulator.cli.gui;

import javax.swing.*;
import java.awt.*;

public class GUI
{
    private final JFrame frame;

    public GUI()
    {
        frame = new JFrame("6502 Emulator");
        frame.setPreferredSize(new Dimension(640, 480));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var menuBar = new JMenuBar();
        var cpuMenu = new JMenu("CPU");
        cpuMenu.add(new JMenuItem("Start"));
        menuBar.add(cpuMenu);
        frame.setJMenuBar(menuBar);

        var cpuMon = new CPUMonitor();
        frame.getContentPane().add(cpuMon.$$$getRootComponent$$$());
    }

    public void show()
    {
        frame.pack();
        frame.setVisible(true);
    }
}
