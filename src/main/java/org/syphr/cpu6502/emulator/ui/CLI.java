package org.syphr.cpu6502.emulator.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.syphr.cpu6502.emulator.machine.Address;
import org.syphr.cpu6502.emulator.machine.CPU;
import org.syphr.cpu6502.emulator.machine.Operation;
import org.syphr.cpu6502.emulator.machine.Program;
import org.syphr.cpu6502.emulator.machine.Value;

import java.util.List;

import static org.syphr.cpu6502.emulator.machine.Operation.*;

@ShellComponent
@RequiredArgsConstructor
public class CLI
{
    private final CPU cpu;

    @ShellMethod(key = "execute")
    public void execute()
    {
        System.out.println("CPU initial state: " + cpu);
        cpu.execute(testProgram());
        System.out.println("CPU final state: " + cpu);
    }

    private Program testProgram()
    {
        List<Operation> operations = List.of(lda(new Value.Decimal(1)),
                                             inc(),
                                             inc(),
                                             pha(),
                                             dec(),
                                             lda(new Address(new Value.Hex("12"), new Value.Hex("34"))),
                                             sta(new Address(new Value.Decimal(2), new Value.Hex("09"))));
        return new Program(operations);
    }
}
