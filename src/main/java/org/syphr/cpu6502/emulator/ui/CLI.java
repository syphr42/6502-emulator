package org.syphr.cpu6502.emulator.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.syphr.cpu6502.emulator.machine.Address;
import org.syphr.cpu6502.emulator.machine.CPU;
import org.syphr.cpu6502.emulator.machine.ClockSpeed;
import org.syphr.cpu6502.emulator.machine.MemoryMap;
import org.syphr.cpu6502.emulator.machine.Operation;
import org.syphr.cpu6502.emulator.machine.Value;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.syphr.cpu6502.emulator.machine.AddressMode.*;
import static org.syphr.cpu6502.emulator.machine.Operation.*;

@ShellComponent
@RequiredArgsConstructor
public class CLI
{
    @ShellMethod(key = "execute")
    public void execute()
    {
        var cpu = new CPU(ClockSpeed.ONE_HZ.times(2), createMemoryMap());

        System.out.println("CPU initial state: " + cpu);
        try {
            cpu.start();
        } finally {
            System.out.println("CPU final state: " + cpu);
        }
    }

    private MemoryMap createMemoryMap()
    {
        var programStart = Address.of(0x00FB);
        List<Operation> operations = List.of(lda(immediate(Value.ZERO)),
                                             beq(relative(Value.of(2))),
                                             inc(accumulator()),
                                             inc(accumulator()),
                                             nop(),
                                             jsr(absolute(Address.of(0x010A))),
                                             jmp(absolute(programStart)),
                                             nop(),
                                             nop(),
                                             inc(accumulator()),
                                             rts());

        Map<Address, Value> memory = toMap(programStart, operations);
        memory.put(Address.RESET, programStart.low());
        memory.put(Address.RESET.increment(), programStart.high());

        return new MemoryMap(memory);
    }

    private Map<Address, Value> toMap(Address start, List<Operation> operations)
    {
        Map<Address, Value> map = new TreeMap<>();

        var address = start;
        for (Operation op : operations) {
            List<Value> values = Operation.toValues(op);
            for (Value value : values) {
                map.put(address, value);
                address = address.increment();
            }
        }

        return map;
    }
}
