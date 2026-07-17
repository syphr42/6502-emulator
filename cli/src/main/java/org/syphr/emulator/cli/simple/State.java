package org.syphr.emulator.cli.simple;

import org.syphr.emulator.common.Value;
import org.syphr.emulator.cpu.CPUState;
import org.syphr.emulator.cpu.Flags;

import java.util.List;

public record State(String programCounter,
                    String accumulator,
                    String x,
                    String y,
                    String stackPointer,
                    List<String> stackData,
                    Flags flags)
{
    public static State from(CPUState cpuState)
    {
        return new State(cpuState.programCounter().toHex(),
                         cpuState.accumulator().toHex(),
                         cpuState.x().toHex(),
                         cpuState.y().toHex(),
                         cpuState.stackPointer().toHex(),
                         cpuState.stackData().stream().map(
                                 Value::toHex).toList(),
                         cpuState.flags());
    }
}
