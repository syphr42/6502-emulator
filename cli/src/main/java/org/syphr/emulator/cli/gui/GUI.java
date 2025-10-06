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
    private final OpLogTableModel opLogData;

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
                        cpuManager.start(addressData.getMemoryMap(), opLogData);
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
        opLogData = new OpLogTableModel();
        var cpuMon = new CPUMonitor(addressData, opLogData);
        frame.getContentPane().add(cpuMon.getRoot());
    }

    public void show()
    {
        frame.pack();
        frame.setVisible(true);
    }
}
