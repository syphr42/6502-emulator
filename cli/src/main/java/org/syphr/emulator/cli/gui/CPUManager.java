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
package org.syphr.emulator.cli.gui;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.syphr.emulator.cli.clock.ClockPeriod;
import org.syphr.emulator.cli.clock.ClockSignal;
import org.syphr.emulator.cpu.Addressable;
import org.syphr.emulator.cpu.CPU;
import org.syphr.emulator.cpu.ClockCycleListener;
import org.syphr.emulator.cpu.OperationListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CPUManager
{
    private final List<CPUManagerListener> listeners = new CopyOnWriteArrayList<>();

    @Getter
    private ClockPeriod clockPeriod = ClockPeriod.of("2hz");

    private @Nullable ClockSignal clockSignal;

    private @Nullable Thread clockThread;
    private @Nullable Thread cpuThread;

    public void addListener(CPUManagerListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(CPUManagerListener listener)
    {
        listeners.remove(listener);
    }

    public void start(Addressable memoryMap, OperationListener opListener, ClockCycleListener cycleListener)
    {
        stop();

        var cpu = CPU.builder().addressable(memoryMap).build();
        cpu.addListener(opListener);
        cpu.addListener(cycleListener);
        cpu.reset();
        cpuThread = new Thread(cpu, "CPU");

        clockSignal = new ClockSignal(clockPeriod.duration(), false, 0);
        clockSignal.addListener(cpu);
        clockThread = new Thread(clockSignal, "Clock");

        cpuThread.start();
        clockThread.start();

        fireCpuStarted();
    }

    public void toggleStepping()
    {
        if (clockSignal != null) {
            clockSignal.toggleStepping();
        }
    }

    public void setClockPeriod(ClockPeriod clockPeriod)
    {
        this.clockPeriod = clockPeriod;

        if (clockSignal != null) {
            clockSignal.setPeriod(clockPeriod.duration());
        }
    }

    public void stop()
    {
        clockSignal = null;

        if (clockThread != null) {
            clockThread.interrupt();
            clockThread = null;
        }

        if (cpuThread != null) {
            cpuThread.interrupt();
            cpuThread = null;
        }

        fireCpuStopped();
    }

    private void fireCpuStarted()
    {
        for (var listener : listeners) {
            listener.cpuStarted();
        }
    }

    private void fireCpuStopped()
    {
        for (var listener : listeners) {
            listener.cpuStopped();
        }
    }
}
