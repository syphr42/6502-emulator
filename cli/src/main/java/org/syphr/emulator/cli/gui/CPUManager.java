package org.syphr.emulator.cli.gui;

import org.jspecify.annotations.Nullable;
import org.syphr.emulator.cli.clock.ClockPeriod;
import org.syphr.emulator.cli.clock.ClockSignal;
import org.syphr.emulator.cpu.Addressable;
import org.syphr.emulator.cpu.CPU;

public class CPUManager
{
    private @Nullable Thread clockThread;
    private @Nullable Thread cpuThread;

    public void start(Addressable memoryMap)
    {
        stop();

        var cpu = CPU.builder().addressable(memoryMap).build();
        cpu.reset();
        cpuThread = new Thread(cpu, "CPU");

        var clockSignal = new ClockSignal(ClockPeriod.of("2hz"), false, 0, cpu);
        clockThread = new Thread(clockSignal, "Clock");

        System.out.println("CPU initial state: " + cpu.getState());
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
