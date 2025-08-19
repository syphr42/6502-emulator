package org.syphr.cpu6502.emulator.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.syphr.cpu6502.emulator.machine.Address;
import org.syphr.cpu6502.emulator.machine.CPU;
import org.syphr.cpu6502.emulator.machine.MemoryMap;
import org.syphr.cpu6502.emulator.machine.Operation;
import org.syphr.cpu6502.emulator.machine.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.syphr.cpu6502.emulator.machine.Operation.*;

@ShellComponent
@RequiredArgsConstructor
public class CLI
{
    @ShellMethod(key = "execute")
    public void execute()
    {
        var cpu = new CPU(createMemoryMap());

        System.out.println("CPU initial state: " + cpu);
        try {
            cpu.start();
        } finally {
            System.out.println("CPU final state: " + cpu);
        }
    }

    private MemoryMap createMemoryMap()
    {
        var programStart = Address.of(0x0000);
        List<Operation> operations = List.of(lda(Value.ZERO),
                                             inc(),
                                             inc(),
                                             nop(),
                                             jmp(programStart));

        Map<Address, Value> memory = new HashMap<>(toMap(programStart, operations));
        memory.put(Address.of(0xFFFC), programStart.low());
        memory.put(Address.of(0xFFFD), programStart.high());

        return new MemoryMap(memory);
    }

    private Map<Address, Value> toMap(Address start, List<Operation> operations)
    {
        Map<Address, Value> map = new HashMap<>();

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
