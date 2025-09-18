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
package org.syphr.emulator.cli.simple;

import org.jline.terminal.Terminal;
import org.jspecify.annotations.Nullable;
import org.syphr.emulator.cli.clock.ClockSignal;
import org.syphr.emulator.cli.memory.MemoryMap;
import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.CPU;

import java.time.Duration;

public class ProgramRunner
{
    private final CPU cpu;

    private final Thread clockThread;
    private final Thread inputThread;
    private final Thread cpuThread;

    public ProgramRunner(Terminal terminal,
                         MemoryMap memoryMap,
                         Duration clockPeriod,
                         boolean stepping,
                         long breakAfterCycle,
                         @Nullable Address executionStart)
    {
        cpu = CPU.builder()
                 .addressable(memoryMap)
                 .start(executionStart)
                 .build();
        if (executionStart == null) {
            cpu.reset();
        }
        cpuThread = new Thread(cpu, "CPU");

        var clockSignal = new ClockSignal(clockPeriod, stepping, breakAfterCycle, cpu);
        clockThread = new Thread(clockSignal, "Clock");

        var inputManager = new InputManager(terminal, clockSignal, new Interrupter(cpu));
        inputThread = new Thread(inputManager, "Input");
    }

    public void run()
    {
        System.out.println("CPU initial state: " + cpu.getState());
        try {
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
}
