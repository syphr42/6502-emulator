package org.syphr.cpu6502.emulator;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

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
        List<Operation> operations = List.of(new Operation.LDA(new Value.Decimal(1)),
                                             new Operation.INC(),
                                             new Operation.INC(),
                                             new Operation.PHA(),
                                             new Operation.DEC(),
                                             new Operation.LDA(new Address(new Value.Hex("12"), new Value.Hex("34"))),
                                             new Operation.STA(new Address(new Value.Decimal(2), new Value.Hex("09"))));
        return new Program(operations);
    }
}
