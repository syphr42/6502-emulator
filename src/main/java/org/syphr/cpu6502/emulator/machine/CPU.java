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
    @ToString.Include
    private final Register accumulator;
    @ToString.Include
    private final Register x;
    @ToString.Include
    private final Register y;

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

    public CPU(ClockSignal clockSignal, AddressHandler addressHandler)
    {
        this(clockSignal, addressHandler, addressHandler);
    }

    public CPU(ClockSignal clockSignal, Reader reader, Writer writer)
    {
        this(new Register(), new Register(), new Register(), new Clock(clockSignal), reader, writer);
    }

    CPU(Register accumulator, Register x, Register y, Clock clock, Reader reader, Writer writer)
    {
        this.accumulator = accumulator;
        this.x = x;
        this.y = y;
        this.clock = clock;
        this.reader = new ClockedReader(reader, clock);
        this.writer = new ClockedWriter(writer, clock);

        stack = new Stack(this.reader, this.writer);
        programManager = new ProgramManager(this.reader);
    }

    public void start()
    {
        var clockThread = new Thread(clock, "Clock");
        clockThread.start();

        try {
            reset();

            while (!Thread.interrupted()) {
                executeNext();
                log.info(getState().toString());
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

    void setProgramCounter(Address address)
    {
        programManager.setProgramCounter(address);
    }

    public Address getStackPointer()
    {
        return stack.getPointer();
    }

    public CPUState getState()
    {
        return new CPUState(getProgramCounter(),
                            accumulator.value(),
                            x.value(),
                            y.value(),
                            stack.getPointer(),
                            stack.getData(),
                            flags);
    }

    private void reset()
    {
        log.info("Resetting CPU");

        // cycle 1: throwaway read
        reader.read(getProgramCounter());

        // cycle 2: throwaway read
        reader.read(getProgramCounter());

        // cycle 3: read stack and decrement pointer
        reader.read(stack.getPointer());
        stack.setPointer(stack.getPointer().low().decrement());

        // cycle 4: read stack and decrement pointer
        reader.read(stack.getPointer());
        stack.setPointer(stack.getPointer().low().decrement());

        // cycle 5: read stack and decrement pointer
        reader.read(stack.getPointer());
        stack.setPointer(stack.getPointer().low().decrement());

        // cycle 6: read low byte of reset vector
        Value low = reader.read(Address.RESET);

        // cycle 7: read high byte of the reset vector
        Value high = reader.read(Address.RESET.increment());

        // set the program counter ready for the first instruction
        programManager.setProgramCounter(Address.of(low, high));
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
            // @formatter:off
            case ADC.ABSOLUTE -> adc(absolute(Address.of(programManager.next(), programManager.next())));
            case ADC.IMMEDIATE -> adc(immediate(programManager.next()));
            case AND.ABSOLUTE -> and(absolute(Address.of(programManager.next(), programManager.next())));
            case AND.IMMEDIATE -> and(immediate(programManager.next()));
            case ASL.ABSOLUTE -> asl(absolute(Address.of(programManager.next(), programManager.next())));
            case ASL.ACCUMULATOR -> { dummyRead(); yield asl(accumulator()); }
            case BCC.RELATIVE -> bcc(relative(programManager.next()));
            case BCS.RELATIVE -> bcs(relative(programManager.next()));
            case BEQ.RELATIVE -> beq(relative(programManager.next()));
            case BIT.ABSOLUTE -> bit(absolute(Address.of(programManager.next(), programManager.next())));
            case BIT.IMMEDIATE -> bit(immediate(programManager.next()));
            case BMI.RELATIVE -> bmi(relative(programManager.next()));
            case BNE.RELATIVE -> bne(relative(programManager.next()));
            case BPL.RELATIVE -> bpl(relative(programManager.next()));
            case BRA.RELATIVE -> bra(relative(programManager.next()));
            case BVC.RELATIVE -> bvc(relative(programManager.next()));
            case BVS.RELATIVE -> bvs(relative(programManager.next()));
            case CLC.IMPLIED -> { dummyRead(); yield clc(); }
            case CLD.IMPLIED -> { dummyRead(); yield cld(); }
            case CLI.IMPLIED -> { dummyRead(); yield cli(); }
            case CLV.IMPLIED -> { dummyRead(); yield clv(); }
            case CMP.ABSOLUTE -> cmp(absolute(Address.of(programManager.next(), programManager.next())));
            case CMP.IMMEDIATE -> cmp(immediate(programManager.next()));
            case CPX.ABSOLUTE -> cpx(absolute(Address.of(programManager.next(), programManager.next())));
            case CPX.IMMEDIATE -> cpx(immediate(programManager.next()));
            case CPY.ABSOLUTE -> cpy(absolute(Address.of(programManager.next(), programManager.next())));
            case CPY.IMMEDIATE -> cpy(immediate(programManager.next()));
            case DEC.ACCUMULATOR -> { dummyRead(); yield dec(accumulator()); }
            case DEX.IMPLIED -> { dummyRead(); yield dex(); }
            case DEY.IMPLIED -> { dummyRead(); yield dey(); }
            case EOR.ABSOLUTE -> eor(absolute(Address.of(programManager.next(), programManager.next())));
            case EOR.IMMEDIATE -> eor(immediate(programManager.next()));
            case INC.ACCUMULATOR -> { dummyRead(); yield inc(accumulator()); }
            case INX.IMPLIED -> { dummyRead(); yield inx(); }
            case INY.IMPLIED -> { dummyRead(); yield iny(); }
            case JMP.ABSOLUTE -> jmp(absolute(Address.of(programManager.next(), programManager.next())));
            case JSR.ABSOLUTE -> jsr(absolute(Address.of(programManager.next(), programManager.next())));
            case LDA.ABSOLUTE -> lda(absolute(Address.of(programManager.next(), programManager.next())));
            case LDA.IMMEDIATE -> lda(immediate(programManager.next()));
            case LDX.ABSOLUTE -> ldx(absolute(Address.of(programManager.next(), programManager.next())));
            case LDX.IMMEDIATE -> ldx(immediate(programManager.next()));
            case LDY.ABSOLUTE -> ldy(absolute(Address.of(programManager.next(), programManager.next())));
            case LDY.IMMEDIATE -> ldy(immediate(programManager.next()));
            case NOP.IMPLIED -> { dummyRead(); yield nop(); }
            case ORA.ABSOLUTE -> ora(absolute(Address.of(programManager.next(), programManager.next())));
            case ORA.IMMEDIATE -> ora(immediate(programManager.next()));
            case PHA.STACK -> { dummyRead(); yield pha(); }
            case PLA.STACK -> { dummyRead(); yield pla(); }
            case ROR.ABSOLUTE -> ror(absolute(Address.of(programManager.next(), programManager.next())));
            case ROR.ACCUMULATOR -> { dummyRead(); yield ror(accumulator()); }
            case RTS.STACK -> { dummyRead(); yield rts(); }
            case STA.ABSOLUTE -> sta(absolute(Address.of(programManager.next(), programManager.next())));
            default -> { log.warn("Unsupported op code: {} (acting as NOP)", opCode); yield nop(); }
            // @formatter:on
        };
    }

    void execute(Operation operation)
    {
        switch (operation) {
            case ADC(AddressMode mode) -> {
                Value value = toValue(mode);
                updateRegister(accumulator, r -> addWithCarry(r, value));
            }
            case AND(AddressMode mode) -> {
                Value value = toValue(mode);
                updateRegister(accumulator, r -> r.store(r.value().and(value)));
            }
            case ASL(AddressMode mode) -> {
                switch (mode) {
                    case Absolute(Address address) -> {
                        reader.read(address); // throw-away read burns a cycle
                        Value input = reader.read(address);
                        Value output = shiftLeft(input);
                        writer.write(address, output);
                        flags = flags.toBuilder().negative(output.isNegative()).zero(output.isZero()).build();
                    }
                    case Accumulator _ -> updateRegister(accumulator, r -> r.store(shiftLeft(r.value())));
                    default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
                }
            }
            case BCC(AddressMode mode) -> branchIf(not(flags::carry), mode);
            case BCS(AddressMode mode) -> branchIf(flags::carry, mode);
            case BEQ(AddressMode mode) -> branchIf(flags::zero, mode);
            case BIT(AddressMode mode) -> {
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
            case BMI(AddressMode mode) -> branchIf(flags::negative, mode);
            case BNE(AddressMode mode) -> branchIf(not(flags::zero), mode);
            case BPL(AddressMode mode) -> branchIf(not(flags::negative), mode);
            case BRA(AddressMode mode) -> branchIf(() -> true, mode);
            case BVC(AddressMode mode) -> branchIf(not(flags::overflow), mode);
            case BVS(AddressMode mode) -> branchIf(flags::overflow, mode);
            case CLC _ -> flags = flags.toBuilder().carry(false).build();
            case CLD _ -> flags = flags.toBuilder().decimal(false).build();
            case CLI _ -> flags = flags.toBuilder().irqDisable(false).build();
            case CLV _ -> flags = flags.toBuilder().overflow(false).build();
            case CMP(AddressMode mode) -> compare(accumulator, toValue(mode));
            case CPX(AddressMode mode) -> compare(x, toValue(mode));
            case CPY(AddressMode mode) -> compare(y, toValue(mode));
            case DEC(AddressMode mode) -> {
                switch (mode) {
                    case Accumulator _ -> updateRegister(accumulator, Register::decrement);
                    default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
                }
            }
            case DEX _ -> updateRegister(x, Register::decrement);
            case DEY _ -> updateRegister(y, Register::decrement);
            case EOR(AddressMode mode) -> updateRegister(accumulator, r -> r.store(r.value().xor(toValue(mode))));
            case INC(AddressMode mode) -> {
                switch (mode) {
                    case Accumulator _ -> updateRegister(accumulator, Register::increment);
                    default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
                }
            }
            case INX _ -> updateRegister(x, Register::increment);
            case INY _ -> updateRegister(y, Register::increment);
            case JMP(AddressMode mode) -> programManager.setProgramCounter(toAddress(mode));
            case JSR(AddressMode mode) -> {
                clock.nextCycle(); // burn a cycle for internal operation
                stack.pushAll(getProgramCounter().decrement().bytes().reversed());
                programManager.setProgramCounter(toAddress(mode));
            }
            case LDA(AddressMode mode) -> updateRegister(accumulator, r -> r.store(toValue(mode)));
            case LDX(AddressMode mode) -> updateRegister(x, r -> r.store(toValue(mode)));
            case LDY(AddressMode mode) -> updateRegister(y, r -> r.store(toValue(mode)));
            case NOP _ -> {}
            case ORA(AddressMode mode) -> updateRegister(accumulator, r -> r.store(r.value().or(toValue(mode))));
            case PHA _ -> pushToStack(accumulator);
            case PLA _ -> {
                clock.nextCycle(); // burn a cycle to increment the stack pointer
                updateRegister(accumulator, this::pullFromStack);
            }
            case ROR(AddressMode mode) -> {
                switch (mode) {
                    case Absolute(Address address) -> {
                        reader.read(address); // throw-away read burns a cycle
                        Value input = reader.read(address);
                        Value output = rotateRight(input);
                        writer.write(address, output);
                        flags = flags.toBuilder().negative(output.isNegative()).zero(output.isZero()).build();
                    }
                    case Accumulator _ -> updateRegister(accumulator, r -> r.store(rotateRight(r.value())));
                    default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
                }
            }
            case RTS _ -> {
                clock.nextCycle(); // burn a cycle to increment the stack pointer
                var address = Address.of(stack.pop(), stack.pop());
                clock.nextCycle(); // burn a cycle to update the PC
                programManager.setProgramCounter(address.increment());
            }
            case STA(AddressMode mode) -> writer.write(toAddress(mode), accumulator.value());
        }
    }

    private Address toAddress(AddressMode mode)
    {
        return switch (mode) {
            case Absolute(Address address) -> address;
            case AbsoluteIndexedIndirectX(Address address) -> {
                var pointer = address.plus(x.value());
                yield Address.of(reader.read(pointer), reader.read(pointer.increment()));
            }
            case AbsoluteIndexedX(Address address) -> address.plus(x.value());
            case AbsoluteIndexedY(Address address) -> address.plus(y.value());
            case AbsoluteIndirect(Address address) ->
                    Address.of(reader.read(address), reader.read(address.increment()));
            case Relative(Value displacement) -> getProgramCounter().plus(displacement);
            case ZeroPage(Value offset) -> Address.ZERO.plus(offset);
            case ZeroPageIndexedIndirectX(Value offset) -> {
                var pointer = Address.ZERO.plus(offset.plus(x.value()));
                yield Address.of(reader.read(pointer), reader.read(pointer.increment()));
            }
            case ZeroPageIndexedX(Value offset) -> Address.ZERO.plus(offset.plus(x.value()));
            case ZeroPageIndexedY(Value offset) -> Address.ZERO.plus(offset.plus(y.value()));
            case ZeroPageIndirect(Value offset) -> {
                var pointer = Address.ZERO.plus(offset);
                yield Address.of(reader.read(pointer), reader.read(pointer.increment()));
            }
            case ZeroPageIndirectIndexedY(Value offset) -> {
                var pointer = Address.ZERO.plus(offset);
                yield Address.of(reader.read(pointer), reader.read(pointer.increment())).plus(y.value());
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

    private Value rotateRight(Value value)
    {
        byte r = value.data();
        byte c = (byte) (flags.carryBit() << 7);
        flags = flags.toBuilder().carry((r & 0x01) != 0).build();

        return Value.of(c | (Byte.toUnsignedInt(r) >> 1));
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

    private void compare(Register register, Value value)
    {
        int compare = Byte.compareUnsigned(register.value().data(), value.data());
        flags = flags.toBuilder()
                     .negative((compare & 0x80) != 0)
                     .zero(compare == 0)
                     .carry(compare >= 0)
                     .build();
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
