/*
 * Copyright © 2025 Gregory P. Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.syphr.emulator.cpu;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.function.Function;

import static org.syphr.emulator.cpu.AddressMode.*;
import static org.syphr.emulator.cpu.Interrupt.HarwareInterrupt.*;
import static org.syphr.emulator.cpu.Interrupt.SoftwareInterrupt.BREAK;
import static org.syphr.emulator.cpu.Operation.*;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@Getter(AccessLevel.PACKAGE)
public class CPU implements Runnable
{
    private final HardwareInterruptState interrupts = new HardwareInterruptState();

    @ToString.Include
    private final Register accumulator;
    @ToString.Include
    private final Register x;
    @ToString.Include
    private final Register y;
    @ToString.Include
    private final StatusRegister status;

    @ToString.Include
    private final Stack stack;

    private final Reader reader;
    private final Writer writer;

    private final Clock clock;
    private final ALU alu;
    private final InstructionDecoder decoder;

    private final ProgramManager programManager;

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        @Nullable
        private Reader reader;
        @Nullable
        private Writer writer;
        @Nullable
        private Address start;

        public Builder addressable(Addressable addressable)
        {
            return reader(addressable).writer(addressable);
        }

        public Builder reader(Reader reader)
        {
            this.reader = reader;
            return this;
        }

        public Builder writer(Writer writer)
        {
            this.writer = writer;
            return this;
        }

        public Builder start(@Nullable Address start)
        {
            this.start = start;
            return this;
        }

        public CPU build()
        {
            return new CPU(Objects.requireNonNull(reader),
                           Objects.requireNonNull(writer),
                           start);
        }
    }

    public CPU(Reader reader, Writer writer, @Nullable Address start)
    {
        this(new Clock(), reader, writer, start);
    }

    CPU(Clock clock, Reader reader, Writer writer, @Nullable Address start)
    {
        this.accumulator = new Register();
        this.x = new Register();
        this.y = new Register();
        this.status = new StatusRegister();
        this.clock = clock;
        this.reader = new ClockedReader(reader, clock);
        this.writer = new ClockedWriter(writer, clock);

        stack = new Stack(this.reader, this.writer);
        programManager = new ProgramManager(this.reader);
        alu = new ALU(status);
        decoder = new InstructionDecoder();

        if (start != null) {
            programManager.setProgramCounter(start);
        }
    }

    public void run()
    {
        try {
            while (!Thread.interrupted()) {
                interrupts.poll().filter(i -> i != IRQ || !status.irqDisable()).ifPresent(this::executeInterrupt);
                executeNext();
            }
        } catch (HaltException e) {
            // stop execution
        }
    }

    public CPUState getState()
    {
        return new CPUState(programManager.getProgramCounter(),
                            accumulator.value(),
                            x.value(),
                            y.value(),
                            stack.getPointer(),
                            stack.getData(),
                            status.flags());
    }

    // --------------- Start External Inputs ------------------

    public long advanceClock()
    {
        return clock.startNextCycle();
    }

    public void reset()
    {
        interrupts.reset();
        log.info("Reset triggered");
    }

    public void interrupt(boolean state)
    {
        interrupts.irq(state);
        log.info("Interrupt state changed: {}", state);
    }

    public void nonMaskableInterrupt()
    {
        interrupts.nmi();
        log.info("Non-maskable interrupt triggered");
    }

    // --------------- End External Inputs ------------------

    void executeInterrupt(Interrupt interrupt)
    {
        log.info("Executing interrupt {}", interrupt);

        var vector = switch (interrupt) {
            case NMI -> Address.NMI;
            case IRQ, BREAK -> Address.IRQ;
            case RESET -> Address.RESET;
        };

        if (BREAK == interrupt) {
            programManager.setProgramCounter(programManager.getProgramCounter().increment());
        } else {
            // cycles 1-2: microcode selection
            clock.awaitNextCycle();
            clock.awaitNextCycle();
        }

        if (RESET == interrupt) {
            // cycle 3: read stack and decrement pointer
            reader.read(stack.getPointer());
            stack.setPointer(stack.getPointer().low().decrement());

            // cycle 4: read stack and decrement pointer
            reader.read(stack.getPointer());
            stack.setPointer(stack.getPointer().low().decrement());

            // cycle 5: read stack and decrement pointer
            reader.read(stack.getPointer());
            stack.setPointer(stack.getPointer().low().decrement());
        } else {
            // cycles 3-4: push program counter to stack
            stack.pushAll(programManager.getProgramCounter().bytes().reversed());

            // cycle 5: push status to stack
            pushToStack(status.copy().setBreakCommand(BREAK == interrupt));
        }

        // cycle 6: read low byte of the vector
        Value low = reader.read(vector);

        // cycle 7: read high byte of the vector
        Value high = reader.read(vector.increment());

        // set the program counter ready for the first instruction
        programManager.setProgramCounter(Address.of(low, high));

        // set flags
        if (RESET == interrupt) {
            status.setUser(true).setBreakCommand(true);
        }
        status.setDecimal(false).setIrqDisable(true);

        log.info(getState().toString());
    }

    void executeNext()
    {
        log.info("Reading next operation");
        Operation op = decoder.nextOp(programManager);
        try (MDC.MDCCloseable _ = MDC.putCloseable("op", op.getClass().getSimpleName())) {
            log.info("Executing op {}", op);
            execute(op);
            log.info("Completed op {}", op);
        }
        log.info(getState().toString());
    }

    void execute(Operation operation)
    {
        switch (operation) {
            case ADC(AddressMode mode) -> alu.addWithCarry(accumulator, toValue(mode));
            case AND(AddressMode mode) -> alu.calculate(accumulator, reg -> reg.and(toValue(mode)));
            case ASL(AddressMode mode) -> readModifyWrite(mode, alu::shiftLeft);
            case BBR0(ZeroPageRelative mode) -> branchIf(!isBitSet(toValue(mode.zp()), 0), mode.relative());
            case BBR1(ZeroPageRelative mode) -> branchIf(!isBitSet(toValue(mode.zp()), 1), mode.relative());
            case BBR2(ZeroPageRelative mode) -> branchIf(!isBitSet(toValue(mode.zp()), 2), mode.relative());
            case BBR3(ZeroPageRelative mode) -> branchIf(!isBitSet(toValue(mode.zp()), 3), mode.relative());
            case BBR4(ZeroPageRelative mode) -> branchIf(!isBitSet(toValue(mode.zp()), 4), mode.relative());
            case BBR5(ZeroPageRelative mode) -> branchIf(!isBitSet(toValue(mode.zp()), 5), mode.relative());
            case BBR6(ZeroPageRelative mode) -> branchIf(!isBitSet(toValue(mode.zp()), 6), mode.relative());
            case BBR7(ZeroPageRelative mode) -> branchIf(!isBitSet(toValue(mode.zp()), 7), mode.relative());
            case BBS0(ZeroPageRelative mode) -> branchIf(isBitSet(toValue(mode.zp()), 0), mode.relative());
            case BBS1(ZeroPageRelative mode) -> branchIf(isBitSet(toValue(mode.zp()), 1), mode.relative());
            case BBS2(ZeroPageRelative mode) -> branchIf(isBitSet(toValue(mode.zp()), 2), mode.relative());
            case BBS3(ZeroPageRelative mode) -> branchIf(isBitSet(toValue(mode.zp()), 3), mode.relative());
            case BBS4(ZeroPageRelative mode) -> branchIf(isBitSet(toValue(mode.zp()), 4), mode.relative());
            case BBS5(ZeroPageRelative mode) -> branchIf(isBitSet(toValue(mode.zp()), 5), mode.relative());
            case BBS6(ZeroPageRelative mode) -> branchIf(isBitSet(toValue(mode.zp()), 6), mode.relative());
            case BBS7(ZeroPageRelative mode) -> branchIf(isBitSet(toValue(mode.zp()), 7), mode.relative());
            case BCC(AddressMode mode) -> branchIf(!status.carry(), mode);
            case BCS(AddressMode mode) -> branchIf(status.carry(), mode);
            case BEQ(AddressMode mode) -> branchIf(status.zero(), mode);
            case BIT(AddressMode mode) -> {
                Value value = toValue(mode);
                if (!(mode instanceof Immediate)) {
                    status.setNegative((value.data() & 0x80) != 0).setOverflow((value.data() & 0x40) != 0);
                }
                status.setZero(accumulator.value().and(value).isZero());
            }
            case BMI(AddressMode mode) -> branchIf(status.negative(), mode);
            case BNE(AddressMode mode) -> branchIf(!status.zero(), mode);
            case BPL(AddressMode mode) -> branchIf(!status.negative(), mode);
            case BRA(AddressMode mode) -> branchIf(true, mode);
            case BRK _ -> executeInterrupt(BREAK);
            case BVC(AddressMode mode) -> branchIf(!status.overflow(), mode);
            case BVS(AddressMode mode) -> branchIf(status.overflow(), mode);
            case CLC _ -> status.setCarry(false);
            case CLD _ -> status.setDecimal(false);
            case CLI _ -> status.setIrqDisable(false);
            case CLV _ -> status.setOverflow(false);
            case CMP(AddressMode mode) -> alu.compare(accumulator, toValue(mode));
            case CPX(AddressMode mode) -> alu.compare(x, toValue(mode));
            case CPY(AddressMode mode) -> alu.compare(y, toValue(mode));
            case DEC(AddressMode mode) -> readModifyWrite(mode, alu::decrement);
            case DEX _ -> alu.calculate(x, Value::decrement);
            case DEY _ -> alu.calculate(y, Value::decrement);
            case EOR(AddressMode mode) -> alu.calculate(accumulator, reg -> reg.xor(toValue(mode)));
            case INC(AddressMode mode) -> readModifyWrite(mode, alu::increment);
            case INX _ -> alu.calculate(x, Value::increment);
            case INY _ -> alu.calculate(y, Value::increment);
            case JMP(AddressMode mode) -> programManager.setProgramCounter(toAddress(mode));
            case JSR(AddressMode mode) -> {
                clock.awaitNextCycle(); // burn a cycle for internal operation
                stack.pushAll(programManager.getProgramCounter().decrement().bytes().reversed());
                programManager.setProgramCounter(toAddress(mode));
            }
            case LDA(AddressMode mode) -> alu.load(accumulator, toValue(mode));
            case LDX(AddressMode mode) -> alu.load(x, toValue(mode));
            case LDY(AddressMode mode) -> alu.load(y, toValue(mode));
            case LSR(AddressMode mode) -> readModifyWrite(mode, alu::shiftRight);
            case NOP _ -> {}
            case ORA(AddressMode mode) -> alu.calculate(accumulator, reg -> reg.or(toValue(mode)));
            case PHA _ -> pushToStack(accumulator);
            case PHP _ -> pushToStack(status);
            case PHX _ -> pushToStack(x);
            case PHY _ -> pushToStack(y);
            case PLA _ -> alu.update(accumulator, this::pullFromStack);
            case PLP _ -> pullFromStack(status);
            case PLX _ -> alu.update(x, this::pullFromStack);
            case PLY _ -> alu.update(y, this::pullFromStack);
            case RMB0(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(0));
            case RMB1(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(1));
            case RMB2(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(2));
            case RMB3(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(3));
            case RMB4(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(4));
            case RMB5(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(5));
            case RMB6(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(6));
            case RMB7(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(7));
            case ROL(AddressMode mode) -> readModifyWrite(mode, alu::rotateLeft);
            case ROR(AddressMode mode) -> readModifyWrite(mode, alu::rotateRight);
            case RTI _ -> {
                pullFromStack(status);
                var address = Address.of(stack.pop(), stack.pop());
                programManager.setProgramCounter(address);
            }
            case RTS _ -> {
                clock.awaitNextCycle(); // burn a cycle to increment the stack pointer
                var address = Address.of(stack.pop(), stack.pop());
                clock.awaitNextCycle(); // burn a cycle to update the PC
                programManager.setProgramCounter(address.increment());
            }
            case SBC(AddressMode mode) -> alu.subtractWithCarry(accumulator, toValue(mode));
            case SEC _ -> status.setCarry(true);
            case SED _ -> status.setDecimal(true);
            case SEI _ -> status.setIrqDisable(true);
            case SMB0(AddressMode mode) -> readModifyWrite(mode, v -> v.set(0));
            case SMB1(AddressMode mode) -> readModifyWrite(mode, v -> v.set(1));
            case SMB2(AddressMode mode) -> readModifyWrite(mode, v -> v.set(2));
            case SMB3(AddressMode mode) -> readModifyWrite(mode, v -> v.set(3));
            case SMB4(AddressMode mode) -> readModifyWrite(mode, v -> v.set(4));
            case SMB5(AddressMode mode) -> readModifyWrite(mode, v -> v.set(5));
            case SMB6(AddressMode mode) -> readModifyWrite(mode, v -> v.set(6));
            case SMB7(AddressMode mode) -> readModifyWrite(mode, v -> v.set(7));
            case STA(AddressMode mode) -> writer.write(toAddress(mode), accumulator.value());
            case STX(AddressMode mode) -> writer.write(toAddress(mode), x.value());
            case STY(AddressMode mode) -> writer.write(toAddress(mode), y.value());
            case STZ(AddressMode mode) -> writer.write(toAddress(mode), Value.ZERO);
            case TAX _ -> alu.load(x, accumulator.value());
            case TAY _ -> alu.load(y, accumulator.value());
            case TRB(AddressMode mode) -> readModifyWrite(mode, v -> {
                status.setZero(accumulator.value().and(v).isZero());
                return accumulator.value().not().and(v);
            });
            case TSB(AddressMode mode) -> readModifyWrite(mode, v -> {
                status.setZero(accumulator.value().and(v).isZero());
                return accumulator.value().or(v);
            });
            case TSX _ -> alu.load(x, stack.getPointer().low());
            case TXA _ -> alu.load(accumulator, x.value());
            case TXS _ -> stack.setPointer(x.value());
            case TYA _ -> alu.load(accumulator, y.value());
        }
    }

    private Address toAddress(AddressMode mode)
    {
        return switch (mode) {
            case Absolute(Address address) -> address;
            case AbsoluteIndexedXIndirect(Address address) -> {
                clock.awaitNextCycle(); // burn a cycle to fix page boundary bug
                var pointer = address.plusUnsigned(x.value());
                yield Address.of(reader.read(pointer), reader.read(pointer.increment()));
            }
            case AbsoluteIndexedX(Address address) -> waitToCrossPageBoundary(address, x.value());
            case AbsoluteIndexedY(Address address) -> waitToCrossPageBoundary(address, y.value());
            case AbsoluteIndirect(Address address) -> {
                clock.awaitNextCycle(); // burn a cycle to fix page boundary bug
                yield Address.of(reader.read(address), reader.read(address.increment()));
            }
            case Relative(Value displacement) ->
                    waitToCrossPageBoundary(programManager.getProgramCounter(), displacement);
            case ZeroPage(Value offset) -> Address.zeroPage(offset);
            case ZeroPageIndexedXIndirect(Value offset) -> {
                throwawayRead(Address.zeroPage(offset));
                var pointer = Address.zeroPage(offset.plus(x.value()));
                yield Address.of(reader.read(pointer), reader.read(pointer.increment()));
            }
            case ZeroPageIndexedX(Value offset) -> {
                throwawayRead(Address.zeroPage(offset));
                yield Address.zeroPage(offset.plus(x.value()));
            }
            case ZeroPageIndexedY(Value offset) -> {
                throwawayRead(Address.zeroPage(offset));
                yield Address.zeroPage(offset.plus(y.value()));
            }
            case ZeroPageIndirect(Value offset) -> {
                var pointer = Address.zeroPage(offset);
                yield Address.of(reader.read(pointer), reader.read(pointer.increment()));
            }
            case ZeroPageIndirectIndexedY(Value offset) -> {
                var pointer = Address.zeroPage(offset);
                Address intermediate = Address.of(reader.read(pointer), reader.read(pointer.increment()));
                yield waitToCrossPageBoundary(intermediate, y.value());
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

    private void throwawayRead(Address address)
    {
        log.info("Performing throwaway read");
        reader.read(address);
    }

    private void readModifyWrite(AddressMode mode, Function<Value, Value> function)
    {
        if (mode instanceof Accumulator) {
            Value output = function.apply(accumulator.value());
            accumulator.load(output);
        } else {
            Address address = toAddress(mode);

            throwawayRead(address);
            Value input = reader.read(address);

            Value output = function.apply(input);
            writer.write(address, output);
        }
    }

    private void pushToStack(Register register)
    {
        stack.push(register.value());
    }

    private void pullFromStack(Register register)
    {
        clock.awaitNextCycle(); // burn a cycle to increment the stack pointer
        register.load(stack.pop());
    }

    private void branchIf(boolean condition, AddressMode mode)
    {
        if (condition) {
            Address target = toAddress(mode);
            clock.awaitNextCycle();
            programManager.setProgramCounter(target);
        }
    }

    private Address waitToCrossPageBoundary(Address address, Value offset)
    {
        Address target = address.plus(offset);

        // wait one cycle if a page boundary will be crossed
        if (!address.high().equals(target.high())) {
            clock.awaitNextCycle();
            log.info("Crossed page boundary");
        }

        return target;
    }

    private boolean isBitSet(Value value, int position)
    {
        // burn a cycle performing the bit test
        clock.awaitNextCycle();

        return value.isSet(position);
    }

    @RequiredArgsConstructor
    private static class ClockedReader implements Reader
    {
        private final Reader reader;
        private final Clock clock;

        public Value read(Address address)
        {
            clock.awaitNextCycle();

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
            clock.awaitNextCycle();

            writer.write(address, value);
            log.info("Wrote {} to {}", value, address);
        }
    }
}
