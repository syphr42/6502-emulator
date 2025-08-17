package org.syphr.cpu6502.emulator.machine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
public class CPU
{
    private static final int DEFAULT_STACK_SIZE = 256;

    private final Register accumulator;

    private final Stack stack;

    @ToString.Exclude
    private final Reader reader;
    @ToString.Exclude
    private final Writer writer;

    @Getter
    private Flags flags = Flags.builder().build();

    public CPU(Reader reader, Writer writer)
    {
        this(DEFAULT_STACK_SIZE, reader, writer);
    }

    public CPU(int stackSize, Reader reader, Writer writer)
    {
        this(new Register(), new Stack(stackSize), reader, writer);
    }

    public void execute(Program program)
    {
        program.operations().forEach(this::execute);
    }

    private void execute(Operation operation)
    {
        switch (operation) {
            case Operation.AND and ->
                    updateRegister(accumulator, r -> r.store(r.value().and(evaluate(and.expression()))));
            case Operation.DEC _ -> updateRegister(accumulator, Register::decrement);
            case Operation.INC _ -> updateRegister(accumulator, Register::increment);
            case Operation.LDA lda -> updateRegister(accumulator, r -> accumulator.store(evaluate(lda.expression())));
            case Operation.ORA ora ->
                    updateRegister(accumulator, r -> r.store(r.value().or(evaluate(ora.expression()))));
            case Operation.PHA _ -> pushToStack(accumulator);
            case Operation.PLA _ -> updateRegister(accumulator, this::pullFromStack);
            case Operation.STA sta -> writer.write(sta.address(), accumulator.value());
        }
    }

    private Value evaluate(Expression expression)
    {
        return switch (expression) {
            case Address address -> reader.read(address);
            case Value val -> val;
        };
    }

    private void updateRegister(Register register, Consumer<Register> action)
    {
        action.accept(register);
        flags = flags.toBuilder().negative(register.isNegative()).zero(register.isZero()).build();
    }

    private void pushToStack(Register register)
    {
        stack.push(register.value());
    }

    private void pullFromStack(Register register)
    {
        updateRegister(register, r -> r.store(stack.pop()));
    }
}
