package org.syphr.cpu6502.emulator.machine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class CPU
{
    private static final int DEFAULT_STACK_SIZE = 256;

    @ToString.Include
    private final Register accumulator;

    @ToString.Include
    private final Stack stack;

    private final Reader reader;
    private final Writer writer;

    private final Clock clock;
    private final ProgramManager programManager;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @ToString.Include
    private Flags flags = Flags.builder().build();

    public CPU(ClockSpeed clockSpeed, AddressHandler addressHandler)
    {
        this(clockSpeed, addressHandler, addressHandler);
    }

    public CPU(ClockSpeed clockSpeed, Reader reader, Writer writer)
    {
        this(DEFAULT_STACK_SIZE, clockSpeed, reader, writer);
    }

    public CPU(int stackSize, ClockSpeed clockSpeed, Reader reader, Writer writer)
    {
        this(new Register(), new Stack(stackSize), new Clock(clockSpeed), reader, writer);
    }

    CPU(Register accumulator, Stack stack, Clock clock, Reader reader, Writer writer)
    {
        this.accumulator = accumulator;
        this.stack = stack;
        this.reader = reader;
        this.writer = writer;
        this.clock = clock;

        programManager = new ProgramManager(clock, reader);
    }

    public void start()
    {
        var clockThread = new Thread(clock, "Clock");
        clockThread.start();

        try {
            var programAddress = Address.of(programManager.next(), programManager.next());
            programManager.jump(programAddress);

            while (!Thread.interrupted()) {
                execute(Operation.next(programManager));
                log.info(this.toString());
            }
        } catch (HaltException e) {
            // stop execution
        } finally {
            clockThread.interrupt();
        }
    }

    public void reset()
    {
        programManager.reset();
    }

    public Address getProgramCounter()
    {
        return programManager.getProgramCounter();
    }

    void execute(Operation operation)
    {
        switch (operation) {
            case Operation.ADC(Expression e) -> updateRegister(accumulator,
                                                               r -> addWithCarry(r, evaluate(e)));
            case Operation.AND(Expression e) -> updateRegister(accumulator, r -> r.store(r.value().and(evaluate(e))));
            case Operation.ASL _ -> updateRegister(accumulator, this::shiftLeft);
            case Operation.BCC(Value v) -> {
                if (!flags.carry()) {
                    programManager.jump(getProgramCounter().plus(v));
                }
            }
            case Operation.BCS(Value v) -> {
                if (flags.carry()) {
                    programManager.jump(getProgramCounter().plus(v));
                }
            }
            case Operation.BEQ(Value v) -> {
                if (flags.zero()) {
                    programManager.jump(getProgramCounter().plus(v));
                }
            }
            case Operation.DEC _ -> updateRegister(accumulator, Register::decrement);
            case Operation.INC _ -> updateRegister(accumulator, Register::increment);
            case Operation.JMP(Address a) -> programManager.jump(a);
            case Operation.JSR(Address a) -> {
                stack.pushAll(getProgramCounter().bytes().reversed());
                programManager.jump(a);
            }
            case Operation.LDA(Expression e) -> updateRegister(accumulator, r -> accumulator.store(evaluate(e)));
            case Operation.NOP _ -> {}
            case Operation.ORA(Expression e) -> updateRegister(accumulator, r -> r.store(r.value().or(evaluate(e))));
            case Operation.PHA _ -> pushToStack(accumulator);
            case Operation.PLA _ -> updateRegister(accumulator, this::pullFromStack);
            case Operation.RTS _ -> {
                var address = Address.of(stack.pop(), stack.pop());
                programManager.jump(address);
            }
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
        register.store(stack.pop());
    }

    private void addWithCarry(Register register, Value value)
    {
        byte r = register.value().data();
        byte v = value.data();
        byte c = flags.carryBit();

        int signedResult = r + v + c;
        boolean overflow = signedResult > Byte.MAX_VALUE || signedResult < Byte.MIN_VALUE;

        int unsignedResult = Byte.toUnsignedInt(r) + Byte.toUnsignedInt(v) + c;
        boolean carry = unsignedResult > 255;

        register.store(Value.of(unsignedResult));
        flags = Flags.builder().overflow(overflow).carry(carry).build();
    }

    private void shiftLeft(Register register)
    {
        byte r = register.value().data();

        register.store(Value.of(r << 1));
        flags = Flags.builder().carry((r & 0x80) != 0).build();
    }
}
