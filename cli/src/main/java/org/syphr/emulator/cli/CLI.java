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
package org.syphr.emulator.cli;

import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.syphr.emulator.cli.clock.ClockPeriod;
import org.syphr.emulator.cli.demo.Programs;
import org.syphr.emulator.cli.gui.GUI;
import org.syphr.emulator.cli.memory.MemoryMap;
import org.syphr.emulator.cli.simple.ProgramRunner;
import org.syphr.emulator.cpu.Address;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

@Command
@RequiredArgsConstructor
public class CLI
{
    private static final String ARG_DESC_BREAK_AFTER_CYCLE = "Switch to stepping mode after the clock executes the given cycle count (counter starts at 1)";
    private static final String ARG_DESC_CLOCK_FREQUENCY = "Frequency at which the clock runs in continuous mode (format: '#unit' where unit is hz, khz, or mhz)";
    private static final String ARG_DESC_EXECUTION_START = "Do not reset the CPU on start and instead begin execution at this address";
    private static final String ARG_DESC_ROM = "Path to binary program file";
    private static final String ARG_DESC_ROM_START = "Start address when a ROM is provided";
    private static final String ARG_DESC_STEPPING = "Start clock in single-step mode (default is continuous mode)";

    private final Terminal terminal;

    @Command(command = "run", description = "Execute a program")
    public void run(@Option(defaultValue = "0", description = ARG_DESC_BREAK_AFTER_CYCLE) long breakAfterCycle,
                    @Option(defaultValue = "2hz", description = ARG_DESC_CLOCK_FREQUENCY) String clockFrequency,
                    @Option(description = ARG_DESC_EXECUTION_START) @Nullable Address executionStart,
                    @Option(description = ARG_DESC_ROM) @Nullable Path rom,
                    @Option(defaultValue = "0x0000", description = ARG_DESC_ROM_START) Address romStart,
                    @Option(defaultValue = "false", description = ARG_DESC_STEPPING) boolean stepping) throws IOException
    {
        if (Terminal.TYPE_DUMB.equals(terminal.getType())) {
            System.out.println("WARNING: Some inputs do not work inside a dumb terminal.");
        }

        MemoryMap memoryMap = rom == null ? Programs.simpleLoopWithSubRoutine() : MemoryMap.of(romStart, rom);
        new ProgramRunner(terminal,
                          memoryMap,
                          ClockPeriod.of(clockFrequency),
                          stepping,
                          breakAfterCycle,
                          executionStart).run();
    }

    @Command(command = "gui", description = "Start the graphical interface")
    public void gui()
    {
        System.setProperty("java.awt.headless", "false");
        var gui = new GUI();
        SwingUtilities.invokeLater(gui::show);
    }
}
