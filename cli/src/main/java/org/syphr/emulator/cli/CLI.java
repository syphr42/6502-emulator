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
import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.CPU;
import org.syphr.emulator.cpu.Operation;
import org.syphr.emulator.cpu.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.syphr.emulator.cpu.AddressMode.*;
import static org.syphr.emulator.cpu.Operation.*;

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

        var cpu = CPU.builder()
                     .addressable(createMemoryMap(romStart, rom))
                     .start(executionStart)
                     .build();

        var clockSignal = new ClockSignal(ClockPeriod.of(clockFrequency), stepping, breakAfterCycle, cpu);
        var clockThread = new Thread(clockSignal, "Clock");

        var inputManager = new InputManager(terminal, clockSignal, new Interrupter(cpu));
        var inputThread = new Thread(inputManager, "Input");

        System.out.println("CPU initial state: " + cpu.getState());
        var cpuThread = new Thread(cpu, "CPU");
        try {
            if (executionStart == null) {
                cpu.reset();
            }

            cpuThread.start();
            clockThread.start();
            inputThread.start();

            cpuThread.join();
        } catch (InterruptedException e) {
            // exit gracefully
        } finally {
            inputThread.interrupt();
            clockThread.interrupt();
            cpuThread.interrupt();
            System.out.println("CPU final state: " + cpu.getState());
        }
    }

    private MemoryMap createMemoryMap(Address romStart, @Nullable Path rom) throws IOException
    {
        if (rom == null) {
            return hardCodedMemoryMap();
        }

        return MemoryMap.of(romStart, rom);
    }

    private MemoryMap hardCodedMemoryMap()
    {
        var programStart = Address.of(0x02FB);
        List<Operation> operations = List.of(lda(immediate(Value.ZERO)),
                                             beq(relative(Value.of(2))),
                                             inc(accumulator()),
                                             inc(accumulator()),
                                             nop(),
                                             jsr(absolute(Address.of(0x030A))),
                                             jmp(absolute(programStart)),
                                             nop(),
                                             nop(),
                                             inc(accumulator()),
                                             rts());

        return MemoryMap.of(programStart, operations);
    }
}
