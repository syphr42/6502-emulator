package org.syphr.cpu6502.emulator.machine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.syphr.cpu6502.emulator.machine.AddressMode.*;
import static org.syphr.cpu6502.emulator.machine.Operation.*;

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
        log.info("Reading next operation");
        Operation op = nextOp();
        log.info("Executing op {}", op);
        execute(op);
        log.info("Completed op {}", op);
    }

    private Operation nextOp()
    {
        Value opCode = programManager.next();
        return switch (opCode.data()) {
            case 0x09 -> ora(immediate(programManager.next()));
            case 0x0A -> asl(accumulator());
            case 0x0D -> ora(absolute(Address.of(programManager.next(), programManager.next())));
            case 0x1A -> inc(accumulator());
            case 0x20 -> jsr(absolute(Address.of(programManager.next(), programManager.next())));
            case 0x2C -> bit(absolute(Address.of(programManager.next(), programManager.next())));
            case 0x29 -> and(immediate(programManager.next()));
            case 0x2D -> and(absolute(Address.of(programManager.next(), programManager.next())));
            case 0x30 -> bmi(relative(programManager.next()));
            case 0x3A -> dec(accumulator());
            case 0x48 -> pha();
            case 0x4C -> jmp(absolute(Address.of(programManager.next(), programManager.next())));
            case 0x60 -> rts();
            case 0x68 -> pla();
            case 0x69 -> adc(immediate(programManager.next()));
            case 0x6D -> adc(absolute(Address.of(programManager.next(), programManager.next())));
            case (byte) 0x89 -> bit(immediate(programManager.next()));
            case (byte) 0x8D -> sta(absolute(Address.of(programManager.next(), programManager.next())));
            case (byte) 0x90 -> bcc(relative(programManager.next()));
            case (byte) 0xA9 -> lda(immediate(programManager.next()));
            case (byte) 0xAD -> lda(absolute(Address.of(programManager.next(), programManager.next())));
            case (byte) 0xB0 -> bcs(relative(programManager.next()));
            case (byte) 0xEA -> nop();
            case (byte) 0xF0 -> beq(relative(programManager.next()));
            default -> {
                log.warn("Unsupported op code: {} (acting as NOP)", opCode);
                yield nop();
            }
        };
    }

    void execute(Operation operation)
    {
        switch (operation) {
            case Operation.ADC(AddressMode mode) -> {
                Value value = toValue(mode);
                updateRegister(accumulator, r -> addWithCarry(r, value));
            }
            case Operation.AND(AddressMode mode) -> {
                Value value = toValue(mode);
                updateRegister(accumulator, r -> r.store(r.value().and(value)));
            }
            case Operation.ASL(AddressMode mode) -> {
                switch (mode) {
                    case Absolute(Address address) -> {
                        reader.read(address); // throw-away read burns a cycle
                        Value value = reader.read(address);
                        writer.write(address, shiftLeft(value));
                    }
                    case Accumulator _ -> {
                        dummyRead();
                        updateRegister(accumulator, r -> r.store(shiftLeft(r.value())));
                    }
                    default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
                }
            }
            case Operation.BCC(AddressMode mode) -> branchIf(not(flags::carry), mode);
            case Operation.BCS(AddressMode mode) -> branchIf(flags::carry, mode);
            case Operation.BEQ(AddressMode mode) -> branchIf(flags::zero, mode);
            case Operation.BIT(AddressMode mode) -> {
                Value value = toValue(mode);
                if (!(mode instanceof Immediate)) {
                    flags = flags.toBuilder()
                                 .negative((value.data() & 0x80) != 0)
                                 .overflow((value.data() & 0x40) != 0)
                                 .build();
                }

                Value and = accumulator.value().and(value);
                if (Value.ZERO.equals(and)) {
                    flags = flags.toBuilder().zero(true).build();
                }
            }
            case Operation.BMI(AddressMode mode) -> branchIf(not(flags::negative), mode);
            case Operation.DEC(AddressMode mode) -> {
                switch (mode) {
                    case Accumulator _ -> {
                        dummyRead();
                        updateRegister(accumulator, Register::decrement);
                    }
                    default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
                }
            }
            case Operation.INC(AddressMode mode) -> {
                switch (mode) {
                    case Accumulator _ -> {
                        dummyRead();
                        updateRegister(accumulator, Register::increment);
                    }
                    default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
                }
            }
            case Operation.JMP(AddressMode mode) -> programManager.setProgramCounter(toAddress(mode));
            case Operation.JSR(AddressMode mode) -> {
                clock.nextCycle(); // extra clock cycle for "internal buffering"?
                stack.pushAll(getProgramCounter().decrement().bytes().reversed());
                programManager.setProgramCounter(toAddress(mode));
            }
            case Operation.LDA(AddressMode mode) -> updateRegister(accumulator, r -> accumulator.store(toValue(mode)));
            case Operation.NOP _ -> dummyRead();
            case Operation.ORA(AddressMode mode) ->
                    updateRegister(accumulator, r -> r.store(r.value().or(toValue(mode))));
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
            case Operation.STA(AddressMode mode) -> writer.write(toAddress(mode), accumulator.value());
        }
    }

    private Address toAddress(AddressMode mode)
    {
        return switch (mode) {
            case Absolute(Address address) -> address;
            case AbsoluteIndirect(Address address) ->
                    Address.of(reader.read(address), reader.read(address.increment()));
            case Relative(Value displacement) -> getProgramCounter().plus(displacement);
            case ZeroPage(Value offset) -> Address.ZERO.plus(offset);
            case ZeroPageIndirect(Value offset) -> {
                Address address = Address.ZERO.plus(offset);
                yield Address.of(reader.read(address), reader.read(address.increment()));
            }
            default -> throw new UnsupportedOperationException("Mode " + mode + " does not support address conversion");
        };
    }

    private Value toValue(AddressMode mode)
    {
        return switch (mode) {
            case Accumulator() -> accumulator.value();
            case Immediate(Value value) -> value;
            default -> reader.read(toAddress(mode));
        };
    }

    private void dummyRead()
    {
        log.info("Performing dummy read");
        reader.read(programManager.getProgramCounter());
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
        flags = flags.toBuilder().overflow(overflow).carry(carry).build();
    }

    private Value shiftLeft(Value value)
    {
        byte r = value.data();
        flags = flags.toBuilder().carry((r & 0x80) != 0).build();

        return Value.of(r << 1);
    }

    private void branchIf(BooleanSupplier flag, AddressMode mode)
    {
        if (flag.getAsBoolean()) {
            Address target = waitToCrossPageBoundary(toAddress(mode));
            clock.nextCycle();
            programManager.setProgramCounter(target);
        }
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

    private BooleanSupplier not(BooleanSupplier supplier)
    {
        return () -> !supplier.getAsBoolean();
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
