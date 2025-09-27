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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.syphr.emulator.common.Value;

import static org.syphr.emulator.cpu.AddressMode.*;
import static org.syphr.emulator.cpu.Operation.*;

@Slf4j
@RequiredArgsConstructor
class InstructionDecoder
{
    public Operation nextOp(ProgramManager programManager)
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

            case TRB.ABSOLUTE -> trb(absolute(programManager.nextAddress()));
            case TRB.ZP -> trb(zp(programManager.nextValue()));

            case TSB.ABSOLUTE -> tsb(absolute(programManager.nextAddress()));
            case TSB.ZP -> tsb(zp(programManager.nextValue()));

            case TSX.IMPLIED -> tsx();
            case TXA.IMPLIED -> txa();
            case TXS.IMPLIED -> txs();
            case TYA.IMPLIED -> tya();

            default -> { log.warn("Unsupported op code: {} (acting as NOP)", opCode); yield nop(); }
            // @formatter:on
        };

        // a throwaway read occurs on all single-byte addressing modes
        switch (op.mode()) {
            case Accumulator _, Implied _, AddressMode.Stack _ -> programManager.read();
            default -> {}
        }

        return op;
    }
}
