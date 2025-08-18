package org.syphr.cpu6502.emulator.machine;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
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

    private final ProgramManager programManager;

    @Getter
    private Flags flags = Flags.builder().build();

    public CPU(AddressHandler addressHandler)
    {
        this(addressHandler, addressHandler);
    }

    public CPU(Reader reader, Writer writer)
    {
        this(DEFAULT_STACK_SIZE, reader, writer);
    }

    public CPU(int stackSize, Reader reader, Writer writer)
    {
        this(new Register(), new Stack(stackSize), reader, writer);
    }

    CPU(Register accumulator, Stack stack, Reader reader, Writer writer)
    {
        this.accumulator = accumulator;
        this.stack = stack;
        this.reader = reader;
        this.writer = writer;

        programManager = new ProgramManager(reader);
    }

    public void start()
    {
        var start = Address.of(programManager.next(), programManager.next());
        programManager.jump(start);

        while (!Thread.interrupted()) {
            execute(Operation.next(programManager));
            log.info(this.toString());
        }
    }

    public void reset()
    {
        programManager.reset();
    }

    void execute(Operation operation)
    {
        switch (operation) {
            case Operation.AND and ->
                    updateRegister(accumulator, r -> r.store(r.value().and(evaluate(and.expression()))));
            case Operation.DEC _ -> updateRegister(accumulator, Register::decrement);
            case Operation.INC _ -> updateRegister(accumulator, Register::increment);
            case Operation.LDA lda -> updateRegister(accumulator, r -> accumulator.store(evaluate(lda.expression())));
            case Operation.NOP _ -> {}
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
