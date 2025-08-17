package org.syphr.cpu6502.emulator;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ToString
public class CPU
{
    private final Register a;

    private final Stack stack;

    public void execute(Program program)
    {
        program.operations().forEach(this::process);
    }

    private void process(Operation operation)
    {
        switch (operation) {
            case Operation.DEC _ -> a.decrement();
            case Operation.INC _ -> a.increment();
            case Operation.LDA lda -> a.store(lda.value());
            case Operation.PHA _ -> pushToStack(a);
            case Operation.PLA _ -> pullFromStack(a);
        }
    }

    private void pushToStack(Register register)
    {
        stack.push(register.value());
    }

    private void pullFromStack(Register register)
    {
        register.store(new Value.Binary(stack.pop()));
    }
}
