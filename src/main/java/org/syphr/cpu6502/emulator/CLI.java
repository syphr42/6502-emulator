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
        List<Operation> operations = List.of(new Operation.LDA(new Value.Decimal(1)), new Operation.INC(), new Operation.INC(), new Operation.DEC());
        var program = new Program(operations);

        System.out.println("CPU initial state: " + cpu);
        cpu.execute(program);
        System.out.println("CPU final state: " + cpu);
    }
}
