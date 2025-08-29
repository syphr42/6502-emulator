package org.syphr.cpu6502.emulator.ui;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.syphr.cpu6502.emulator.machine.Address;
import org.syphr.cpu6502.emulator.machine.CPU;
import org.syphr.cpu6502.emulator.machine.ClockSpeed;
import org.syphr.cpu6502.emulator.machine.Operation;
import org.syphr.cpu6502.emulator.machine.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.syphr.cpu6502.emulator.machine.AddressMode.*;
import static org.syphr.cpu6502.emulator.machine.Operation.*;

@Command
@RequiredArgsConstructor
public class CLI
{
    @Command(command = "execute")
    public void execute(@Option(defaultValue = "2hz") String clock,
                        @Option @Nullable Path rom,
                        @Option @Nullable Address romStart) throws IOException
    {
        var clockSpeed = ClockSpeed.of(clock);
        var cpu = new CPU(clockSpeed, createMemoryMap(romStart, rom));

        System.out.println("CPU initial state: " + cpu.getState());
        try {
            cpu.start();
        } finally {
            System.out.println("CPU final state: " + cpu.getState());
        }
    }

    private MemoryMap createMemoryMap(@Nullable Address romStart, @Nullable Path rom) throws IOException
    {
        if (rom == null && romStart == null) {
            return hardCodedMemoryMap();
        }

        if (rom == null || romStart == null) {
            throw new IllegalArgumentException("rom and romStart must be supplied together");
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
