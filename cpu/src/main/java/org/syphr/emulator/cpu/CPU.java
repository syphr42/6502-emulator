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
import org.slf4j.MDC;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.syphr.emulator.cpu.AddressMode.*;
import static org.syphr.emulator.cpu.Operation.*;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@Getter(AccessLevel.PACKAGE)
public class CPU
{
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
    private final ProgramManager programManager;

    public CPU(ClockSignal clockSignal, AddressHandler addressHandler)
    {
        this(clockSignal, addressHandler, addressHandler);
    }

    public CPU(ClockSignal clockSignal, Reader reader, Writer writer)
    {
        this(new Clock(clockSignal), reader, writer);
    }

    CPU(Clock clock, Reader reader, Writer writer)
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

    private void reset()
    {
        log.info("Resetting CPU");

        // cycle 1: throwaway read
        reader.read(programManager.getProgramCounter());

        // cycle 2: throwaway read
        reader.read(programManager.getProgramCounter());

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

        // set hardware-initialized status flags
        status.setUser(true).setBreakCommand(true).setDecimal(false).setIrqDisable(true);
    }

    void executeNext()
    {
        log.info("Reading next operation");
        Operation op = nextOp();
        log.info("Executing op {}", op);
        try (MDC.MDCCloseable _ = MDC.putCloseable("op", op.getClass().getSimpleName())) {
            execute(op);
        }
        log.info("Completed op {}", op);
    }

    private Operation nextOp()
    {
        Value opCode = programManager.nextValue();
        Operation op = switch (opCode.data()) {
            // @formatter:off
            case ADC.ABSOLUTE -> adc(absolute(programManager.nextAddress()));
            case ADC.ABSOLUTE_X -> adc(absoluteX(programManager.nextAddress()));
            case ADC.ABSOLUTE_Y -> adc(absoluteY(programManager.nextAddress()));
            case ADC.IMMEDIATE -> adc(immediate(programManager.nextValue()));
            case ADC.ZP -> adc(zp(programManager.nextValue()));
            case ADC.ZP_X_INDIRECT -> adc(zpXIndirect(programManager.nextValue()));
            case ADC.ZP_X -> adc(zpX(programManager.nextValue()));
            case ADC.ZP_INDIRECT -> adc(zpIndirect(programManager.nextValue()));
            case ADC.ZP_INDIRECT_Y -> adc(zpIndirectY(programManager.nextValue()));

            case AND.ABSOLUTE -> and(absolute(programManager.nextAddress()));
            case AND.ABSOLUTE_X -> and(absoluteX(programManager.nextAddress()));
            case AND.ABSOLUTE_Y -> and(absoluteY(programManager.nextAddress()));
            case AND.IMMEDIATE -> and(immediate(programManager.nextValue()));
            case AND.ZP -> and(zp(programManager.nextValue()));
            case AND.ZP_X_INDIRECT -> and(zpXIndirect(programManager.nextValue()));
            case AND.ZP_X -> and(zpX(programManager.nextValue()));
            case AND.ZP_INDIRECT -> and(zpIndirect(programManager.nextValue()));
            case AND.ZP_INDIRECT_Y -> and(zpIndirectY(programManager.nextValue()));

            case ASL.ABSOLUTE -> asl(absolute(programManager.nextAddress()));
            case ASL.ABSOLUTE_X -> asl(absoluteX(programManager.nextAddress()));
            case ASL.ACCUMULATOR -> asl(accumulator());
            case ASL.ZP -> asl(zp(programManager.nextValue()));
            case ASL.ZP_X -> asl(zpX(programManager.nextValue()));

            case BBR0.ZP_RELATIVE -> bbr0(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBR1.ZP_RELATIVE -> bbr1(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBR2.ZP_RELATIVE -> bbr2(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBR3.ZP_RELATIVE -> bbr3(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBR4.ZP_RELATIVE -> bbr4(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBR5.ZP_RELATIVE -> bbr5(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBR6.ZP_RELATIVE -> bbr6(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBR7.ZP_RELATIVE -> bbr7(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));

            case BBS0.ZP_RELATIVE -> bbs0(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBS1.ZP_RELATIVE -> bbs1(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBS2.ZP_RELATIVE -> bbs2(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBS3.ZP_RELATIVE -> bbs3(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBS4.ZP_RELATIVE -> bbs4(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBS5.ZP_RELATIVE -> bbs5(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBS6.ZP_RELATIVE -> bbs6(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));
            case BBS7.ZP_RELATIVE -> bbs7(zpRelative(zp(programManager.nextValue()), relative(programManager.nextValue())));

            case BCC.RELATIVE -> bcc(relative(programManager.nextValue()));
            case BCS.RELATIVE -> bcs(relative(programManager.nextValue()));
            case BEQ.RELATIVE -> beq(relative(programManager.nextValue()));

            case BIT.ABSOLUTE -> bit(absolute(programManager.nextAddress()));
            case BIT.ABSOLUTE_X -> bit(absoluteX(programManager.nextAddress()));
            case BIT.IMMEDIATE -> bit(immediate(programManager.nextValue()));
            case BIT.ZP -> bit(zp(programManager.nextValue()));
            case BIT.ZP_X -> bit(zpX(programManager.nextValue()));

            case BMI.RELATIVE -> bmi(relative(programManager.nextValue()));
            case BNE.RELATIVE -> bne(relative(programManager.nextValue()));
            case BPL.RELATIVE -> bpl(relative(programManager.nextValue()));
            case BRA.RELATIVE -> bra(relative(programManager.nextValue()));

            case BRK.STACK -> brk();

            case BVC.RELATIVE -> bvc(relative(programManager.nextValue()));
            case BVS.RELATIVE -> bvs(relative(programManager.nextValue()));

            case CLC.IMPLIED -> clc();
            case CLD.IMPLIED -> cld();
            case CLI.IMPLIED -> cli();
            case CLV.IMPLIED -> clv();

            case CMP.ABSOLUTE -> cmp(absolute(programManager.nextAddress()));
            case CMP.ABSOLUTE_X -> cmp(absoluteX(programManager.nextAddress()));
            case CMP.ABSOLUTE_Y -> cmp(absoluteY(programManager.nextAddress()));
            case CMP.IMMEDIATE -> cmp(immediate(programManager.nextValue()));
            case CMP.ZP -> cmp(zp(programManager.nextValue()));
            case CMP.ZP_X_INDIRECT -> cmp(zpXIndirect(programManager.nextValue()));
            case CMP.ZP_X -> cmp(zpX(programManager.nextValue()));
            case CMP.ZP_INDIRECT -> cmp(zpIndirect(programManager.nextValue()));
            case CMP.ZP_INDIRECT_Y -> cmp(zpIndirectY(programManager.nextValue()));

            case CPX.ABSOLUTE -> cpx(absolute(programManager.nextAddress()));
            case CPX.IMMEDIATE -> cpx(immediate(programManager.nextValue()));
            case CPX.ZP -> cpx(zp(programManager.nextValue()));

            case CPY.ABSOLUTE -> cpy(absolute(programManager.nextAddress()));
            case CPY.IMMEDIATE -> cpy(immediate(programManager.nextValue()));
            case CPY.ZP -> cpy(zp(programManager.nextValue()));

            case DEC.ABSOLUTE -> dec(absolute(programManager.nextAddress()));
            case DEC.ABSOLUTE_X -> dec(absoluteX(programManager.nextAddress()));
            case DEC.ACCUMULATOR -> dec(accumulator());
            case DEC.ZP -> dec(zp(programManager.nextValue()));
            case DEC.ZP_X -> dec(zpX(programManager.nextValue()));

            case DEX.IMPLIED -> dex();
            case DEY.IMPLIED -> dey();

            case EOR.ABSOLUTE -> eor(absolute(programManager.nextAddress()));
            case EOR.ABSOLUTE_X -> eor(absoluteX(programManager.nextAddress()));
            case EOR.ABSOLUTE_Y -> eor(absoluteY(programManager.nextAddress()));
            case EOR.IMMEDIATE -> eor(immediate(programManager.nextValue()));
            case EOR.ZP -> eor(zp(programManager.nextValue()));
            case EOR.ZP_X_INDIRECT -> eor(zpXIndirect(programManager.nextValue()));
            case EOR.ZP_X -> eor(zpX(programManager.nextValue()));
            case EOR.ZP_INDIRECT -> eor(zpIndirect(programManager.nextValue()));
            case EOR.ZP_INDIRECT_Y -> eor(zpIndirectY(programManager.nextValue()));

            case INC.ABSOLUTE -> inc(absolute(programManager.nextAddress()));
            case INC.ABSOLUTE_X -> inc(absoluteX(programManager.nextAddress()));
            case INC.ACCUMULATOR -> inc(accumulator());
            case INC.ZP -> inc(zp(programManager.nextValue()));
            case INC.ZP_X -> inc(zpX(programManager.nextValue()));

            case INX.IMPLIED -> inx();
            case INY.IMPLIED -> iny();

            case JMP.ABSOLUTE -> jmp(absolute(programManager.nextAddress()));
            case JMP.ABSOLUTE_X_INDIRECT -> jmp(absoluteXIndirect(programManager.nextAddress()));
            case JMP.ABSOLUTE_INDIRECT -> jmp(absoluteIndirect(programManager.nextAddress()));

            case JSR.ABSOLUTE -> jsr(absolute(programManager.nextAddress()));

            case LDA.ABSOLUTE -> lda(absolute(programManager.nextAddress()));
            case LDA.ABSOLUTE_X -> lda(absoluteX(programManager.nextAddress()));
            case LDA.ABSOLUTE_Y -> lda(absoluteY(programManager.nextAddress()));
            case LDA.IMMEDIATE -> lda(immediate(programManager.nextValue()));
            case LDA.ZP -> lda(zp(programManager.nextValue()));
            case LDA.ZP_X_INDIRECT -> lda(zpXIndirect(programManager.nextValue()));
            case LDA.ZP_X -> lda(zpX(programManager.nextValue()));
            case LDA.ZP_INDIRECT -> lda(zpIndirect(programManager.nextValue()));
            case LDA.ZP_INDIRECT_Y -> lda(zpIndirectY(programManager.nextValue()));

            case LDX.ABSOLUTE -> ldx(absolute(programManager.nextAddress()));
            case LDX.ABSOLUTE_Y -> ldx(absoluteY(programManager.nextAddress()));
            case LDX.IMMEDIATE -> ldx(immediate(programManager.nextValue()));
            case LDX.ZP -> ldx(zp(programManager.nextValue()));
            case LDX.ZP_Y -> ldx(zpY(programManager.nextValue()));

            case LDY.ABSOLUTE -> ldy(absolute(programManager.nextAddress()));
            case LDY.ABSOLUTE_X -> ldy(absoluteX(programManager.nextAddress()));
            case LDY.IMMEDIATE -> ldy(immediate(programManager.nextValue()));
            case LDY.ZP -> ldy(zp(programManager.nextValue()));
            case LDY.ZP_X -> ldy(zpX(programManager.nextValue()));

            case LSR.ABSOLUTE -> lsr(absolute(programManager.nextAddress()));
            case LSR.ABSOLUTE_X -> lsr(absoluteX(programManager.nextAddress()));
            case LSR.ACCUMULATOR -> lsr(accumulator());
            case LSR.ZP -> lsr(zp(programManager.nextValue()));
            case LSR.ZP_X -> lsr(zpX(programManager.nextValue()));

            case NOP.IMPLIED -> nop();

            case ORA.ABSOLUTE -> ora(absolute(programManager.nextAddress()));
            case ORA.ABSOLUTE_X -> ora(absoluteX(programManager.nextAddress()));
            case ORA.ABSOLUTE_Y -> ora(absoluteY(programManager.nextAddress()));
            case ORA.IMMEDIATE -> ora(immediate(programManager.nextValue()));
            case ORA.ZP -> ora(zp(programManager.nextValue()));
            case ORA.ZP_X_INDIRECT -> ora(zpXIndirect(programManager.nextValue()));
            case ORA.ZP_X -> ora(zpX(programManager.nextValue()));
            case ORA.ZP_INDIRECT -> ora(zpIndirect(programManager.nextValue()));
            case ORA.ZP_INDIRECT_Y -> ora(zpIndirectY(programManager.nextValue()));

            case PHA.STACK -> pha();
            case PHP.STACK -> php();
            case PHX.STACK -> phx();
            case PHY.STACK -> phy();

            case PLA.STACK -> pla();
            case PLP.STACK -> plp();
            case PLX.STACK -> plx();
            case PLY.STACK -> ply();

            case RMB0.ZP -> rmb0(zp(programManager.nextValue()));
            case RMB1.ZP -> rmb1(zp(programManager.nextValue()));
            case RMB2.ZP -> rmb2(zp(programManager.nextValue()));
            case RMB3.ZP -> rmb3(zp(programManager.nextValue()));
            case RMB4.ZP -> rmb4(zp(programManager.nextValue()));
            case RMB5.ZP -> rmb5(zp(programManager.nextValue()));
            case RMB6.ZP -> rmb6(zp(programManager.nextValue()));
            case RMB7.ZP -> rmb7(zp(programManager.nextValue()));

            case ROL.ABSOLUTE -> rol(absolute(programManager.nextAddress()));
            case ROL.ABSOLUTE_X -> rol(absoluteX(programManager.nextAddress()));
            case ROL.ACCUMULATOR -> rol(accumulator());
            case ROL.ZP -> rol(zp(programManager.nextValue()));
            case ROL.ZP_X -> rol(zpX(programManager.nextValue()));

            case ROR.ABSOLUTE -> ror(absolute(programManager.nextAddress()));
            case ROR.ABSOLUTE_X -> ror(absoluteX(programManager.nextAddress()));
            case ROR.ACCUMULATOR -> ror(accumulator());
            case ROR.ZP -> ror(zp(programManager.nextValue()));
            case ROR.ZP_X -> ror(zpX(programManager.nextValue()));

            case RTI.STACK -> rti();
            case RTS.STACK -> rts();

            case SBC.ABSOLUTE -> sbc(absolute(programManager.nextAddress()));
            case SBC.ABSOLUTE_X -> sbc(absoluteX(programManager.nextAddress()));
            case SBC.ABSOLUTE_Y -> sbc(absoluteY(programManager.nextAddress()));
            case SBC.IMMEDIATE -> sbc(immediate(programManager.nextValue()));
            case SBC.ZP -> sbc(zp(programManager.nextValue()));
            case SBC.ZP_X_INDIRECT -> sbc(zpXIndirect(programManager.nextValue()));
            case SBC.ZP_X -> sbc(zpX(programManager.nextValue()));
            case SBC.ZP_INDIRECT -> sbc(zpIndirect(programManager.nextValue()));
            case SBC.ZP_INDIRECT_Y -> sbc(zpIndirectY(programManager.nextValue()));

            case SEC.IMPLIED -> sec();
            case SED.IMPLIED -> sed();
            case SEI.IMPLIED -> sei();

            case SMB0.ZP -> smb0(zp(programManager.nextValue()));
            case SMB1.ZP -> smb1(zp(programManager.nextValue()));
            case SMB2.ZP -> smb2(zp(programManager.nextValue()));
            case SMB3.ZP -> smb3(zp(programManager.nextValue()));
            case SMB4.ZP -> smb4(zp(programManager.nextValue()));
            case SMB5.ZP -> smb5(zp(programManager.nextValue()));
            case SMB6.ZP -> smb6(zp(programManager.nextValue()));
            case SMB7.ZP -> smb7(zp(programManager.nextValue()));

            case STA.ABSOLUTE -> sta(absolute(programManager.nextAddress()));
            case STA.ABSOLUTE_X -> sta(absoluteX(programManager.nextAddress()));
            case STA.ABSOLUTE_Y -> sta(absoluteY(programManager.nextAddress()));
            case STA.ZP -> sta(zp(programManager.nextValue()));
            case STA.ZP_X_INDIRECT -> sta(zpXIndirect(programManager.nextValue()));
            case STA.ZP_X -> sta(zpX(programManager.nextValue()));
            case STA.ZP_INDIRECT -> sta(zpIndirect(programManager.nextValue()));
            case STA.ZP_INDIRECT_Y -> sta(zpIndirectY(programManager.nextValue()));

            case STX.ABSOLUTE -> stx(absolute(programManager.nextAddress()));
            case STX.ZP -> stx(zp(programManager.nextValue()));
            case STX.ZP_Y -> stx(zpY(programManager.nextValue()));

            case STY.ABSOLUTE -> sty(absolute(programManager.nextAddress()));
            case STY.ZP -> sty(zp(programManager.nextValue()));
            case STY.ZP_X -> sty(zpX(programManager.nextValue()));

            case STZ.ABSOLUTE -> stz(absolute(programManager.nextAddress()));
            case STZ.ABSOLUTE_X -> stz(absoluteX(programManager.nextAddress()));
            case STZ.ZP -> stz(zp(programManager.nextValue()));
            case STZ.ZP_X -> stz(zpX(programManager.nextValue()));

            case TAX.IMPLIED -> tax();
            case TAY.IMPLIED -> tay();
            case TSX.IMPLIED -> tsx();
            case TXA.IMPLIED -> txa();
            case TXS.IMPLIED -> txs();
            case TYA.IMPLIED -> tya();

            default -> { log.warn("Unsupported op code: {} (acting as NOP)", opCode); yield nop(); }
            // @formatter:on
        };

        // a throwaway read occurs on all single-byte addressing modes
        switch (op.mode()) {
            case Accumulator _, Implied _, AddressMode.Stack _ -> dummyRead();
            default -> {}
        }

        return op;
    }

    void execute(Operation operation)
    {
        switch (operation) {
            case ADC(AddressMode mode) -> updateRegister(accumulator, r -> addWithCarry(r, toValue(mode)));
            case AND(AddressMode mode) -> updateRegister(accumulator, r -> r.store(r.value().and(toValue(mode))));
            case ASL(AddressMode mode) -> readModifyWriteWithFlags(mode, this::shiftLeft);
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
            case BRK _ -> {
                stack.pushAll(programManager.getProgramCounter().increment().bytes().reversed());
                pushToStack(status.copy().setBreakCommand(true));
                programManager.setProgramCounter(Address.of(reader.read(Address.IRQ),
                                                            reader.read(Address.IRQ.increment())));
                status.setDecimal(false).setIrqDisable(true);
            }
            case BVC(AddressMode mode) -> branchIf(!status.overflow(), mode);
            case BVS(AddressMode mode) -> branchIf(status.overflow(), mode);
            case CLC _ -> status.setCarry(false);
            case CLD _ -> status.setDecimal(false);
            case CLI _ -> status.setIrqDisable(false);
            case CLV _ -> status.setOverflow(false);
            case CMP(AddressMode mode) -> compare(accumulator, toValue(mode));
            case CPX(AddressMode mode) -> compare(x, toValue(mode));
            case CPY(AddressMode mode) -> compare(y, toValue(mode));
            case DEC(AddressMode mode) -> readModifyWriteWithFlags(mode, Value::decrement);
            case DEX _ -> updateRegister(x, Register::decrement);
            case DEY _ -> updateRegister(y, Register::decrement);
            case EOR(AddressMode mode) -> updateRegister(accumulator, r -> r.store(r.value().xor(toValue(mode))));
            case INC(AddressMode mode) -> readModifyWriteWithFlags(mode, Value::increment);
            case INX _ -> updateRegister(x, Register::increment);
            case INY _ -> updateRegister(y, Register::increment);
            case JMP(AddressMode mode) -> programManager.setProgramCounter(toAddress(mode));
            case JSR(AddressMode mode) -> {
                clock.nextCycle(); // burn a cycle for internal operation
                stack.pushAll(programManager.getProgramCounter().decrement().bytes().reversed());
                programManager.setProgramCounter(toAddress(mode));
            }
            case LDA(AddressMode mode) -> updateRegister(accumulator, r -> r.store(toValue(mode)));
            case LDX(AddressMode mode) -> updateRegister(x, r -> r.store(toValue(mode)));
            case LDY(AddressMode mode) -> updateRegister(y, r -> r.store(toValue(mode)));
            case LSR(AddressMode mode) -> readModifyWriteWithFlags(mode, this::shiftRight);
            case NOP _ -> {}
            case ORA(AddressMode mode) -> updateRegister(accumulator, r -> r.store(r.value().or(toValue(mode))));
            case PHA _ -> pushToStack(accumulator);
            case PHP _ -> pushToStack(status);
            case PHX _ -> pushToStack(x);
            case PHY _ -> pushToStack(y);
            case PLA _ -> updateRegister(accumulator, this::pullFromStack);
            case PLP _ -> pullFromStack(status);
            case PLX _ -> updateRegister(x, this::pullFromStack);
            case PLY _ -> updateRegister(y, this::pullFromStack);
            case RMB0(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(0));
            case RMB1(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(1));
            case RMB2(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(2));
            case RMB3(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(3));
            case RMB4(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(4));
            case RMB5(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(5));
            case RMB6(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(6));
            case RMB7(AddressMode mode) -> readModifyWrite(mode, v -> v.clear(7));
            case ROL(AddressMode mode) -> readModifyWriteWithFlags(mode, this::rotateLeft);
            case ROR(AddressMode mode) -> readModifyWriteWithFlags(mode, this::rotateRight);
            case RTI _ -> {
                pullFromStack(status);
                var address = Address.of(stack.pop(), stack.pop());
                programManager.setProgramCounter(address);
            }
            case RTS _ -> {
                clock.nextCycle(); // burn a cycle to increment the stack pointer
                var address = Address.of(stack.pop(), stack.pop());
                clock.nextCycle(); // burn a cycle to update the PC
                programManager.setProgramCounter(address.increment());
            }
            case SBC(AddressMode mode) -> updateRegister(accumulator, r -> subtractWithCarry(r, toValue(mode)));
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
            case TAX _ -> updateRegister(x, r -> r.store(accumulator.value()));
            case TAY _ -> updateRegister(y, r -> r.store(accumulator.value()));
            case TSX _ -> updateRegister(x, r -> r.store(stack.getPointer().low()));
            case TXA _ -> updateRegister(accumulator, r -> r.store(x.value()));
            case TXS _ -> stack.setPointer(x.value());
            case TYA _ -> updateRegister(accumulator, r -> r.store(y.value()));
        }
    }

    private Address toAddress(AddressMode mode)
    {
        return switch (mode) {
            case Absolute(Address address) -> address;
            case AbsoluteIndexedXIndirect(Address address) -> {
                clock.nextCycle(); // burn a cycle to fix page boundary bug
                var pointer = address.plusUnsigned(x.value());
                yield Address.of(reader.read(pointer), reader.read(pointer.increment()));
            }
            case AbsoluteIndexedX(Address address) -> waitToCrossPageBoundary(address, x.value());
            case AbsoluteIndexedY(Address address) -> waitToCrossPageBoundary(address, y.value());
            case AbsoluteIndirect(Address address) -> {
                clock.nextCycle(); // burn a cycle to fix page boundary bug
                yield Address.of(reader.read(address), reader.read(address.increment()));
            }
            case Relative(Value displacement) ->
                    waitToCrossPageBoundary(programManager.getProgramCounter(), displacement);
            case ZeroPage(Value offset) -> Address.zeroPage(offset);
            case ZeroPageIndexedXIndirect(Value offset) -> {
                reader.read(Address.zeroPage(offset)); // throwaway read before index is applied
                var pointer = Address.zeroPage(offset.plus(x.value()));
                yield Address.of(reader.read(pointer), reader.read(pointer.increment()));
            }
            case ZeroPageIndexedX(Value offset) -> {
                reader.read(Address.zeroPage(offset)); // throwaway read before index is applied
                yield Address.zeroPage(offset.plus(x.value()));
            }
            case ZeroPageIndexedY(Value offset) -> {
                reader.read(Address.zeroPage(offset)); // throwaway read before index is applied
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

    private void dummyRead()
    {
        log.info("Performing dummy read");
        reader.read(programManager.getProgramCounter());
    }

    private void readModifyWriteWithFlags(AddressMode mode, Function<Value, Value> function)
    {
        Value result = readModifyWrite(mode, function);
        status.setNegative(result.isNegative()).setZero(result.isZero());
    }

    private Value readModifyWrite(AddressMode mode, Function<Value, Value> function)
    {
        if (mode instanceof Accumulator) {
            Value output = function.apply(accumulator.value());
            accumulator.store(output);

            return output;
        } else {
            Address address = toAddress(mode);

            reader.read(address); // throw-away read burns a cycle
            Value input = reader.read(address);

            Value output = function.apply(input);
            writer.write(address, output);

            return output;
        }
    }

    private void updateRegister(Register register, Consumer<Register> action)
    {
        action.accept(register);
        status.setNegative(register.isNegative()).setZero(register.isZero());
    }

    private void pushToStack(Register register)
    {
        stack.push(register.value());
    }

    private void pullFromStack(Register register)
    {
        clock.nextCycle(); // burn a cycle to increment the stack pointer
        register.store(stack.pop());
    }

    private void addWithCarry(Register register, Value value)
    {
        byte r = register.value().data();
        byte v = value.data();
        byte c = (byte) (status.carry() ? 0x01 : 0x00);

        int signedResult = r + v + c;
        boolean overflow = signedResult > Byte.MAX_VALUE || signedResult < Byte.MIN_VALUE;

        int unsignedResult = Byte.toUnsignedInt(r) + Byte.toUnsignedInt(v) + c;
        boolean carry = unsignedResult > 255;

        register.store(Value.of(unsignedResult));
        status.setOverflow(overflow).setCarry(carry);
    }

    private void subtractWithCarry(Register register, Value value)
    {
        byte r = register.value().data();
        byte v = value.data();
        byte c = (byte) (status.carry() ? 0x00 : 0x01);

        int signedResult = r - v - c;
        boolean overflow = signedResult > Byte.MAX_VALUE || signedResult < Byte.MIN_VALUE;

        int unsignedResult = Byte.toUnsignedInt(r) - Byte.toUnsignedInt(v) - Byte.toUnsignedInt(c);
        boolean carry = unsignedResult >= 0;

        register.store(Value.of(unsignedResult));
        status.setOverflow(overflow).setCarry(carry);
    }

    private Value shiftLeft(Value value)
    {
        byte r = value.data();
        status.setCarry((r & 0x80) != 0);

        return Value.of(r << 1);
    }

    private Value shiftRight(Value value)
    {
        byte r = value.data();
        status.setCarry((r & 0x01) != 0);

        return Value.of(Byte.toUnsignedInt(r) >> 1);
    }

    private Value rotateLeft(Value value)
    {
        byte r = value.data();
        byte c = (byte) (status.carry() ? 0x01 : 0x00);
        status.setCarry((r & 0x80) != 0);

        return Value.of(c | (r << 1));
    }

    private Value rotateRight(Value value)
    {
        byte r = value.data();
        byte c = (byte) (status.carry() ? 0x80 : 0x00);
        status.setCarry((r & 0x01) != 0);

        return Value.of(c | (Byte.toUnsignedInt(r) >> 1));
    }

    private void branchIf(boolean condition, AddressMode mode)
    {
        if (condition) {
            Address target = toAddress(mode);
            clock.nextCycle();
            programManager.setProgramCounter(target);
        }
    }

    private Address waitToCrossPageBoundary(Address address, Value offset)
    {
        Address target = address.plus(offset);

        // wait one cycle if a page boundary will be crossed
        if (!address.high().equals(target.high())) {
            clock.nextCycle();
            log.info("Crossed page boundary");
        }

        return target;
    }

    private boolean isBitSet(Value value, int position)
    {
        // burn a cycle performing the bit test
        clock.nextCycle();

        return value.isSet(position);
    }

    private void compare(Register register, Value value)
    {
        int compare = Byte.compareUnsigned(register.value().data(), value.data());
        status.setNegative((compare & 0x80) != 0).setZero(compare == 0).setCarry(compare >= 0);
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
