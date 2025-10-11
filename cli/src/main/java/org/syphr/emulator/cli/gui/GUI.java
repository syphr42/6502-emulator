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

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.syphr.emulator.cli.demo.Programs;
import org.syphr.emulator.cli.memory.MemoryMap;
import org.syphr.emulator.cpu.CPUEvent.ClockCycleEvent;
import org.syphr.emulator.cpu.CPUEvent.OperationEvent;
import org.syphr.emulator.cpu.ClockCycleListener;
import org.syphr.emulator.cpu.OperationListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@RequiredArgsConstructor
public class GUI
{
    private final CPUManager cpuManager;

    private final AddressTableModel addressData = new AddressTableModel();
    private final OpLogTableModel opLogData = new OpLogTableModel();
    private final CycleLogTableModel cycleLogData = new CycleLogTableModel();

    public void show()
    {
        var stopCpuAction = new AbstractAction("Stop")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new SwingWorker<>()
                {
                    @Override
                    protected @Nullable Object doInBackground()
                    {
                        cpuManager.stop();
                        return null;
                    }
                }.execute();
            }
        };

        setLookAndFeel();

        var frame = new JFrame("6502 Emulator");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                stopCpuAction.actionPerformed(new ActionEvent(frame, ActionEvent.ACTION_PERFORMED, "window closed"));
            }
        });

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
        addressingMenu.add(new AbstractAction("Load ROM")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                var dialog = new ROMSelectionDialog(frame, GUI.this::changeMemoryMap);
                dialog.setVisible(true);
            }
        });
        addressingMenu.add(new AbstractAction("Load Demo Program")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                changeMemoryMap(Programs.simpleLoopWithSubRoutine());
            }
        });
        menuBar.add(addressingMenu);
        var cpuMenu = new JMenu("CPU");
        cpuMenu.add(new AbstractAction("Start")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                opLogData.clear();
                cycleLogData.clear();

                new SwingWorker<>()
                {
                    @Override
                    protected @Nullable Object doInBackground()
                    {
                        var opListener = new OperationListener()
                        {
                            @Override
                            public void operationCompleted(OperationEvent event)
                            {
                                SwingUtilities.invokeLater(() -> opLogData.addEvent(event));
                            }
                        };
                        var cycleListener = new ClockCycleListener()
                        {
                            @Override
                            public void clockCycleCompleted(ClockCycleEvent event)
                            {
                                SwingUtilities.invokeLater(() -> cycleLogData.addEvent(event));
                            }
                        };
                        cpuManager.start(addressData.getMemoryMap(), opListener, cycleListener);
                        return null;
                    }
                }.execute();
            }
        });
        cpuMenu.add(stopCpuAction);
        menuBar.add(cpuMenu);
        frame.setJMenuBar(menuBar);

        var cpuMon = new CPUMonitor(addressData, opLogData, cycleLogData);
        frame.getContentPane().add(cpuMon.getRoot());

        frame.pack();
        frame.setVisible(true);
    }

    private void setLookAndFeel()
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            // this should not be possible
            throw new RuntimeException(e);
        }
    }

    private void changeMemoryMap(MemoryMap memoryMap)
    {
        cpuManager.stop();
        opLogData.clear();
        cycleLogData.clear();
        addressData.loadMemoryMap(memoryMap);
    }
}
