/*
 * Copyright © 2025-2026 Gregory P. Moyer
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
package org.syphr.emulator.cli;

import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import org.syphr.emulator.cli.clock.ClockPeriod;
import org.syphr.emulator.cli.demo.Programs;
import org.syphr.emulator.cli.gui.CPUManager;
import org.syphr.emulator.cli.gui.GUI;
import org.syphr.emulator.cli.memory.MemoryMap;
import org.syphr.emulator.cli.simple.ProgramRunner;
import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.Breakpoint;
import org.syphr.emulator.cpu.ClockCycleBreakpoint;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CLI
{
    private static final String ARG_DESC_BREAK_AFTER_CYCLE = "Switch to stepping mode after the clock executes the given cycle count (counter starts at 1)";
    private static final String ARG_DESC_CLOCK_FREQUENCY = "Frequency at which the clock runs in continuous mode (format: '#unit' where unit is hz, khz, or mhz)";
    private static final String ARG_DESC_EXECUTION_START = "Do not reset the CPU on start and instead begin execution at this address";
    private static final String ARG_DESC_BIN = "Path to binary data file";
    private static final String ARG_DESC_BIN_START = "Start address for provided binary data";
    private static final String ARG_DESC_BIN_WRITABLE = "Allow the binary data to be writable in memory";
    private static final String ARG_DESC_STEPPING = "Start clock in single-step mode (default is continuous mode)";

    private final Terminal terminal;

    @Command(name = "run", description = "Execute a program")
    public void run(@Option(defaultValue = "0", description = ARG_DESC_BREAK_AFTER_CYCLE, longName = "break-after-cycle") long breakAfterCycle,
                    @Option(defaultValue = "2hz", description = ARG_DESC_CLOCK_FREQUENCY, longName = "clock-frequency") String clockFrequency,
                    @Option(description = ARG_DESC_EXECUTION_START, longName = "execution-start") @Nullable Address executionStart,
                    @Option(description = ARG_DESC_BIN, longName = "bin") @Nullable Path bin,
                    @Option(defaultValue = "0x0000", description = ARG_DESC_BIN_START, longName = "bin-start") Address binStart,
                    @Option(defaultValue = "false", description = ARG_DESC_BIN_WRITABLE, longName = "bin-writable") boolean binWritable,
                    @Option(defaultValue = "false", description = ARG_DESC_STEPPING, longName = "stepping") boolean stepping) throws IOException
    {
        if (Terminal.TYPE_DUMB.equals(terminal.getType())) {
            System.out.println("WARNING: Some inputs do not work inside a dumb terminal.");
        }

        MemoryMap memoryMap = bin == null
                              ? Programs.simpleLoopWithSubRoutine()
                              : MemoryMap.of(binStart, bin, binWritable);

        List<Breakpoint> breakpoints = new ArrayList<>();
        if (breakAfterCycle > 0) {
            breakpoints.add(new ClockCycleBreakpoint(breakAfterCycle));
        }

        new ProgramRunner(terminal,
                          memoryMap,
                          ClockPeriod.of(clockFrequency),
                          stepping,
                          breakpoints,
                          executionStart).run();
    }

    @Command(name = "gui", description = "Start the graphical interface")
    public void gui()
    {
        System.setProperty("java.awt.headless", "false");
        SwingUtilities.invokeLater(() -> {
            var gui = new GUI(new CPUManager());
            gui.show();
        });
    }
}
