package org.syphr.cpu6502.emulator.machine;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ToString
public class CPU
{
    private final Register accumulator;

    private final Stack stack;

    @ToString.Exclude
    private final Reader reader;
    @ToString.Exclude
    private final Writer writer;

    public void execute(Program program)
    {
        program.operations().forEach(this::process);
    }

    private void process(Operation operation)
    {
        switch (operation) {
            case Operation.DEC _ -> accumulator.decrement();
            case Operation.INC _ -> accumulator.increment();
            case Operation.LDA lda -> accumulator.store(evaluate(lda.expression()));
            case Operation.PHA _ -> pushToStack(accumulator);
            case Operation.PLA _ -> pullFromStack(accumulator);
            case Operation.STA sta -> writer.write(sta.address(), accumulator.value());
        }
    }

    private Value evaluate(Expression expression)
    {
        return switch (expression) {
            case Address addr -> new Value.Decimal(reader.read(addr));
            case Value val -> val;
        };
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
