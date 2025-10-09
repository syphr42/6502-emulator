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
import org.syphr.emulator.cli.clock.ClockPeriod;
import org.syphr.emulator.cli.clock.ClockSignal;
import org.syphr.emulator.cpu.Addressable;
import org.syphr.emulator.cpu.CPU;
import org.syphr.emulator.cpu.OperationListener;

public class CPUManager
{
    private @Nullable Thread clockThread;
    private @Nullable Thread cpuThread;

    public void start(Addressable memoryMap, OperationListener listener)
    {
        stop();

        var clockSignal = new ClockSignal(ClockPeriod.of("2hz"), false, 0);
        clockThread = new Thread(clockSignal, "Clock");

        var cpu = CPU.builder().clockGenerator(clockSignal).addressable(memoryMap).build();
        cpu.addListener(listener);
        cpu.reset();
        cpuThread = new Thread(cpu, "CPU");

        cpuThread.start();
        clockThread.start();
    }

    public void stop()
    {
        if (clockThread != null) {
            clockThread.interrupt();
            clockThread = null;
        }

        if (cpuThread != null) {
            cpuThread.interrupt();
            cpuThread = null;
        }
    }
}
