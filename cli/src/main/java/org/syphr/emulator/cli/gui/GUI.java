package org.syphr.emulator.cli.gui;

import org.jspecify.annotations.Nullable;
import org.syphr.emulator.cli.demo.Programs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GUI
{
    private final CPUManager cpuManager;
    private final JFrame frame;
    private final AddressTableModel addressData;

    public GUI()
    {
        cpuManager = new CPUManager();

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
        cpuMenu.add(new AbstractAction("Start")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new SwingWorker<Object, Object>()
                {
                    @Override
                    protected @Nullable Object doInBackground() throws Exception
                    {
                        cpuManager.start(addressData.getMemoryMap());
                        return null;
                    }
                }.execute();
            }
        });
        cpuMenu.add(new AbstractAction("Stop")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new SwingWorker<Object, Object>()
                {
                    @Override
                    protected @Nullable Object doInBackground() throws Exception
                    {
                        cpuManager.stop();
                        return null;
                    }
                }.execute();
            }
        });
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
