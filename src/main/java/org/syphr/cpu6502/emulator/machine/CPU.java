package org.syphr.cpu6502.emulator.machine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
    private Flags flags = Flags.builder().user(true).breakCommand(true).decimal(false).irqDisable(true).build();

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
        this(stackSize, new Clock(clockSpeed), reader, writer);
    }

    private CPU(int stackSize, Clock clock, Reader reader, Writer writer)
    {
        this(new Register(), new Stack(stackSize, clock), clock, reader, writer);
    }

    CPU(Register accumulator, Stack stack, Clock clock, Reader reader, Writer writer)
    {
        this.accumulator = accumulator;
        this.stack = stack;
        this.reader = new ClockedReader(reader, clock);
        this.writer = new ClockedWriter(writer, clock);
        this.clock = clock;

        programManager = new ProgramManager(this.reader);
    }

    public void start()
    {
        var clockThread = new Thread(clock, "Clock");
        clockThread.start();

        // mimic startup actions
        log.info("Performing startup sequence");
        clock.nextCycle();
        clock.nextCycle();
        clock.nextCycle();
        clock.nextCycle();
        clock.nextCycle();

        try {
            var programAddress = Address.of(programManager.next(), programManager.next());
            programManager.setProgramCounter(programAddress);

            while (!Thread.interrupted()) {
                executeNext();
                log.info(this.toString());
            }
        } catch (HaltException e) {
            // stop execution
        } finally {
            clockThread.interrupt();
        }
    }

    public Address getProgramCounter()
    {
        return programManager.getProgramCounter();
    }

    void executeNext()
    {
        var op = Operation.next(programManager);
        log.info("Executing op {}", op);
        execute(op);
        log.info("Completed op {}", op);
    }

    void execute(Operation operation)
    {
        switch (operation) {
            case Operation.ADC(Expression e) -> updateRegister(accumulator,
                                                               r -> addWithCarry(r, evaluate(e)));
            case Operation.AND(Expression e) -> updateRegister(accumulator, r -> r.store(r.value().and(evaluate(e))));
            case Operation.ASL _ -> {
                dummyRead();
                updateRegister(accumulator, this::shiftLeft);
            }
            case Operation.BCC(Value v) -> {
                if (!flags.carry()) {
                    Address target = waitToCrossPageBoundary(getProgramCounter().plus(v));
                    clock.nextCycle();
                    programManager.setProgramCounter(target);
                }
            }
            case Operation.BCS(Value v) -> {
                if (flags.carry()) {
                    Address target = waitToCrossPageBoundary(getProgramCounter().plus(v));
                    clock.nextCycle();
                    programManager.setProgramCounter(target);
                }
            }
            case Operation.BEQ(Value v) -> {
                if (flags.zero()) {
                    Address target = waitToCrossPageBoundary(getProgramCounter().plus(v));
                    clock.nextCycle();
                    programManager.setProgramCounter(target);
                }
            }
            case Operation.DEC _ -> {
                dummyRead();
                updateRegister(accumulator, Register::decrement);
            }
            case Operation.INC _ -> {
                dummyRead();
                updateRegister(accumulator, Register::increment);
            }
            case Operation.JMP(Address a) -> programManager.setProgramCounter(a);
            case Operation.JSR(Address a) -> {
                clock.nextCycle(); // extra clock cycle for "internal buffering"?
                stack.pushAll(getProgramCounter().decrement().bytes().reversed());
                programManager.setProgramCounter(a);
            }
            case Operation.LDA(Expression e) -> updateRegister(accumulator, r -> accumulator.store(evaluate(e)));
            case Operation.NOP _ -> dummyRead();
            case Operation.ORA(Expression e) -> updateRegister(accumulator, r -> r.store(r.value().or(evaluate(e))));
            case Operation.PHA _ -> {
                dummyRead();
                pushToStack(accumulator);
            }
            case Operation.PLA _ -> {
                dummyRead();
                clock.nextCycle(); // unexplained
                updateRegister(accumulator, this::pullFromStack);
            }
            case Operation.RTS _ -> {
                dummyRead();
                var address = Address.of(stack.pop(), stack.pop());
                clock.nextCycle(); // unexplained
                clock.nextCycle(); // unexplained
                programManager.setProgramCounter(address.increment());
            }
            case Operation.STA sta -> writer.write(sta.address(), accumulator.value());
        }
    }

    private void dummyRead()
    {
        log.info("Performing dummy read");
        reader.read(programManager.getProgramCounter());
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

    private Address waitToCrossPageBoundary(Address target)
    {
        // wait one cycle if a page boundary will be crossed
        if (!getProgramCounter().high().equals(target.high())) {
            clock.nextCycle();
            log.info("Crossed page boundary");
        }

        return target;
    }

    @RequiredArgsConstructor
    private static class ClockedReader implements Reader
    {
        private final Reader reader;
        private final Clock clock;

        public Value read(Address address)
        {
            clock.nextCycle();

            Value value = reader.read(address);
            log.info("Read {} from {}", value, address);

            return value;
        }
    }

    @RequiredArgsConstructor
    private static class ClockedWriter implements Writer
    {
        private final Writer writer;
        private final Clock clock;

        public void write(Address address, Value value)
        {
            clock.nextCycle();

            writer.write(address, value);
            log.info("Wrote {} to {}", value, address);
        }
    }
}
