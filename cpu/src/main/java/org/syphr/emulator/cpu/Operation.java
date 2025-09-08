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

import java.util.List;
import java.util.stream.Stream;

import static org.syphr.emulator.cpu.AddressMode.*;

public sealed interface Operation
{
    Value code();

    AddressMode mode();

    record ADC(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x6D;
        public static final byte ABSOLUTE_X = 0x7D;
        public static final byte ABSOLUTE_Y = 0x79;
        public static final byte IMMEDIATE = 0x69;
        public static final byte ZP = 0x65;
        public static final byte ZP_X_INDIRECT = 0x61;
        public static final byte ZP_X = 0x75;
        public static final byte ZP_INDIRECT = 0x72;
        public static final byte ZP_INDIRECT_Y = 0x71;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedXIndirect _ -> ZP_X_INDIRECT;
                case ZeroPageIndexedX _ -> ZP_X;
                case ZeroPageIndirect _ -> ZP_INDIRECT;
                case ZeroPageIndirectIndexedY _ -> ZP_INDIRECT_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record AND(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x2D;
        public static final byte ABSOLUTE_X = 0x3D;
        public static final byte ABSOLUTE_Y = 0x39;
        public static final byte IMMEDIATE = 0x29;
        public static final byte ZP = 0x25;
        public static final byte ZP_X_INDIRECT = 0x21;
        public static final byte ZP_X = 0x35;
        public static final byte ZP_INDIRECT = 0x32;
        public static final byte ZP_INDIRECT_Y = 0x31;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedXIndirect _ -> ZP_X_INDIRECT;
                case ZeroPageIndexedX _ -> ZP_X;
                case ZeroPageIndirect _ -> ZP_INDIRECT;
                case ZeroPageIndirectIndexedY _ -> ZP_INDIRECT_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record ASL(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x0E;
        public static final byte ABSOLUTE_X = 0x1E;
        public static final byte ACCUMULATOR = 0x0A;
        public static final byte ZP = 0x06;
        public static final byte ZP_X = 0x16;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case Accumulator _ -> ACCUMULATOR;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record BBR0(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = 0x0F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBR1(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = 0x1F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBR2(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = 0x2F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBR3(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = 0x3F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBR4(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = 0x4F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBR5(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = 0x5F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBR6(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = 0x6F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBR7(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = 0x7F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBS0(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = (byte) 0x8F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBS1(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = (byte) 0x9F;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBS2(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = (byte) 0xAF;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBS3(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = (byte) 0xBF;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBS4(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = (byte) 0xCF;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBS5(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = (byte) 0xDF;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBS6(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = (byte) 0xEF;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BBS7(ZeroPageRelative mode) implements Operation
    {
        public static final byte ZP_RELATIVE = (byte) 0xFF;

        public Value code()
        {
            return Value.of(ZP_RELATIVE);
        }
    }

    record BCC(Relative mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x90;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record BCS(Relative mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xB0;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record BEQ(Relative mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xF0;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record BIT(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x2C;
        public static final byte ABSOLUTE_X = 0x3C;
        public static final byte IMMEDIATE = (byte) 0x89;
        public static final byte ZP = 0x24;
        public static final byte ZP_X = 0x34;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record BMI(Relative mode) implements Operation
    {
        public static final byte RELATIVE = 0x30;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record BNE(Relative mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xD0;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record BPL(Relative mode) implements Operation
    {
        public static final byte RELATIVE = 0x10;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record BRA(Relative mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x80;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record BRK() implements Operation
    {
        public static final byte STACK = 0x00;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record BVC(Relative mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x50;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record BVS(Relative mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x70;

        public Value code()
        {
            return Value.of(RELATIVE);
        }
    }

    record CLC() implements Operation
    {
        public static final byte IMPLIED = 0x18;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record CLD() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xD8;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record CLI() implements Operation
    {
        public static final byte IMPLIED = 0x58;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record CLV() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xB8;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record CMP(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xCD;
        public static final byte ABSOLUTE_X = (byte) 0xDD;
        public static final byte ABSOLUTE_Y = (byte) 0xD9;
        public static final byte IMMEDIATE = (byte) 0xC9;
        public static final byte ZP = (byte) 0xC5;
        public static final byte ZP_X_INDIRECT = (byte) 0xC1;
        public static final byte ZP_X = (byte) 0xD5;
        public static final byte ZP_INDIRECT = (byte) 0xD2;
        public static final byte ZP_INDIRECT_Y = (byte) 0xD1;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedXIndirect _ -> ZP_X_INDIRECT;
                case ZeroPageIndexedX _ -> ZP_X;
                case ZeroPageIndirect _ -> ZP_INDIRECT;
                case ZeroPageIndirectIndexedY _ -> ZP_INDIRECT_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record CPX(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xEC;
        public static final byte IMMEDIATE = (byte) 0xE0;
        public static final byte ZP = (byte) 0xE4;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                default -> throw unsupported(this);
            });
        }
    }

    record CPY(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xCC;
        public static final byte IMMEDIATE = (byte) 0xC0;
        public static final byte ZP = (byte) 0xC4;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                default -> throw unsupported(this);
            });
        }
    }

    record DEC(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xCE;
        public static final byte ABSOLUTE_X = (byte) 0xDE;
        public static final byte ACCUMULATOR = 0x3A;
        public static final byte ZP = (byte) 0xC6;
        public static final byte ZP_X = (byte) 0xD6;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case Accumulator _ -> ACCUMULATOR;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record DEX() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xCA;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record DEY() implements Operation
    {
        public static final byte IMPLIED = (byte) 0x88;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record EOR(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x4D;
        public static final byte ABSOLUTE_X = 0x5D;
        public static final byte ABSOLUTE_Y = 0x59;
        public static final byte IMMEDIATE = 0x49;
        public static final byte ZP = 0x45;
        public static final byte ZP_X_INDIRECT = 0x41;
        public static final byte ZP_X = 0x55;
        public static final byte ZP_INDIRECT = 0x52;
        public static final byte ZP_INDIRECT_Y = 0x51;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedXIndirect _ -> ZP_X_INDIRECT;
                case ZeroPageIndexedX _ -> ZP_X;
                case ZeroPageIndirect _ -> ZP_INDIRECT;
                case ZeroPageIndirectIndexedY _ -> ZP_INDIRECT_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record INC(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xEE;
        public static final byte ABSOLUTE_X = (byte) 0xFE;
        public static final byte ACCUMULATOR = 0x1A;
        public static final byte ZP = (byte) 0xE6;
        public static final byte ZP_X = (byte) 0xF6;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case Accumulator _ -> ACCUMULATOR;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record INX() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xE8;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record INY() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xC8;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record JMP(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x4C;
        public static final byte ABSOLUTE_X_INDIRECT = 0x7C;
        public static final byte ABSOLUTE_INDIRECT = 0x6C;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedXIndirect _ -> ABSOLUTE_X_INDIRECT;
                case AbsoluteIndirect _ -> ABSOLUTE_INDIRECT;
                default -> throw unsupported(this);
            });
        }
    }

    record JSR(Absolute mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x20;

        public Value code()
        {
            return Value.of(ABSOLUTE);
        }
    }

    record LDA(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xAD;
        public static final byte ABSOLUTE_X = (byte) 0xBD;
        public static final byte ABSOLUTE_Y = (byte) 0xB9;
        public static final byte IMMEDIATE = (byte) 0xA9;
        public static final byte ZP = (byte) 0xA5;
        public static final byte ZP_X_INDIRECT = (byte) 0xA1;
        public static final byte ZP_X = (byte) 0xB5;
        public static final byte ZP_INDIRECT = (byte) 0xB2;
        public static final byte ZP_INDIRECT_Y = (byte) 0xB1;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedXIndirect _ -> ZP_X_INDIRECT;
                case ZeroPageIndexedX _ -> ZP_X;
                case ZeroPageIndirect _ -> ZP_INDIRECT;
                case ZeroPageIndirectIndexedY _ -> ZP_INDIRECT_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record LDX(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xAE;
        public static final byte ABSOLUTE_Y = (byte) 0xBE;
        public static final byte IMMEDIATE = (byte) 0xA2;
        public static final byte ZP = (byte) 0xA6;
        public static final byte ZP_Y = (byte) 0xB6;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedY _ -> ZP_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record LDY(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xAC;
        public static final byte ABSOLUTE_X = (byte) 0xBC;
        public static final byte IMMEDIATE = (byte) 0xA0;
        public static final byte ZP = (byte) 0xA4;
        public static final byte ZP_X = (byte) 0xB4;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record LSR(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x4E;
        public static final byte ABSOLUTE_X = 0x5E;
        public static final byte ACCUMULATOR = 0x4A;
        public static final byte ZP = 0x46;
        public static final byte ZP_X = 0x56;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case Accumulator _ -> ACCUMULATOR;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record NOP() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xEA;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record ORA(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x0D;
        public static final byte ABSOLUTE_X = 0x1D;
        public static final byte ABSOLUTE_Y = 0x19;
        public static final byte IMMEDIATE = 0x09;
        public static final byte ZP = 0x05;
        public static final byte ZP_X_INDIRECT = 0x01;
        public static final byte ZP_X = 0x15;
        public static final byte ZP_INDIRECT = 0x12;
        public static final byte ZP_INDIRECT_Y = 0x11;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedXIndirect _ -> ZP_X_INDIRECT;
                case ZeroPageIndexedX _ -> ZP_X;
                case ZeroPageIndirect _ -> ZP_INDIRECT;
                case ZeroPageIndirectIndexedY _ -> ZP_INDIRECT_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record PHA() implements Operation
    {
        public static final byte STACK = 0x48;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record PHP() implements Operation
    {
        public static final byte STACK = 0x08;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record PHX() implements Operation
    {
        public static final byte STACK = (byte) 0xDA;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record PHY() implements Operation
    {
        public static final byte STACK = 0x5A;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record PLA() implements Operation
    {
        public static final byte STACK = 0x68;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record PLP() implements Operation
    {
        public static final byte STACK = 0x28;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record PLX() implements Operation
    {
        public static final byte STACK = (byte) 0xFA;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record PLY() implements Operation
    {
        public static final byte STACK = 0x7A;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record RMB0(ZeroPage mode) implements Operation
    {
        public static final byte ZP = 0x07;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record RMB1(ZeroPage mode) implements Operation
    {
        public static final byte ZP = 0x17;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record RMB2(ZeroPage mode) implements Operation
    {
        public static final byte ZP = 0x27;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record RMB3(ZeroPage mode) implements Operation
    {
        public static final byte ZP = 0x37;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record RMB4(ZeroPage mode) implements Operation
    {
        public static final byte ZP = 0x47;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record RMB5(ZeroPage mode) implements Operation
    {
        public static final byte ZP = 0x57;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record RMB6(ZeroPage mode) implements Operation
    {
        public static final byte ZP = 0x67;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record RMB7(ZeroPage mode) implements Operation
    {
        public static final byte ZP = 0x77;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record ROL(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x2E;
        public static final byte ABSOLUTE_X = 0x3E;
        public static final byte ACCUMULATOR = 0x2A;
        public static final byte ZP = 0x26;
        public static final byte ZP_X = 0x36;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case Accumulator _ -> ACCUMULATOR;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record ROR(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x6E;
        public static final byte ABSOLUTE_X = 0x7E;
        public static final byte ACCUMULATOR = 0x6A;
        public static final byte ZP = 0x66;
        public static final byte ZP_X = 0x76;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case Accumulator _ -> ACCUMULATOR;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record RTI() implements Operation
    {
        public static final byte STACK = 0x40;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record RTS() implements Operation
    {
        public static final byte STACK = 0x60;

        public AddressMode mode()
        {
            return stack();
        }

        public Value code()
        {
            return Value.of(STACK);
        }
    }

    record SBC(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xED;
        public static final byte ABSOLUTE_X = (byte) 0xFD;
        public static final byte ABSOLUTE_Y = (byte) 0xF9;
        public static final byte IMMEDIATE = (byte) 0xE9;
        public static final byte ZP = (byte) 0xE5;
        public static final byte ZP_X_INDIRECT = (byte) 0xE1;
        public static final byte ZP_X = (byte) 0xF5;
        public static final byte ZP_INDIRECT = (byte) 0xF2;
        public static final byte ZP_INDIRECT_Y = (byte) 0xF1;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case Immediate _ -> IMMEDIATE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedXIndirect _ -> ZP_X_INDIRECT;
                case ZeroPageIndexedX _ -> ZP_X;
                case ZeroPageIndirect _ -> ZP_INDIRECT;
                case ZeroPageIndirectIndexedY _ -> ZP_INDIRECT_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record SEC() implements Operation
    {
        public static final byte IMPLIED = 0x38;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record SED() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xF8;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record SEI() implements Operation
    {
        public static final byte IMPLIED = 0x78;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record SMB0(ZeroPage mode) implements Operation
    {
        public static final byte ZP = (byte) 0x87;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record SMB1(ZeroPage mode) implements Operation
    {
        public static final byte ZP = (byte) 0x97;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record SMB2(ZeroPage mode) implements Operation
    {
        public static final byte ZP = (byte) 0xA7;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record SMB3(ZeroPage mode) implements Operation
    {
        public static final byte ZP = (byte) 0xB7;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record SMB4(ZeroPage mode) implements Operation
    {
        public static final byte ZP = (byte) 0xC7;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record SMB5(ZeroPage mode) implements Operation
    {
        public static final byte ZP = (byte) 0xD7;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record SMB6(ZeroPage mode) implements Operation
    {
        public static final byte ZP = (byte) 0xE7;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record SMB7(ZeroPage mode) implements Operation
    {
        public static final byte ZP = (byte) 0xF7;

        public Value code()
        {
            return Value.of(ZP);
        }
    }

    record STA(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0x8D;
        public static final byte ABSOLUTE_X = (byte) 0x9D;
        public static final byte ABSOLUTE_Y = (byte) 0x99;
        public static final byte ZP = (byte) 0x85;
        public static final byte ZP_X_INDIRECT = (byte) 0x81;
        public static final byte ZP_X = (byte) 0x95;
        public static final byte ZP_INDIRECT = (byte) 0x92;
        public static final byte ZP_INDIRECT_Y = (byte) 0x91;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case AbsoluteIndexedY _ -> ABSOLUTE_Y;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedXIndirect _ -> ZP_X_INDIRECT;
                case ZeroPageIndexedX _ -> ZP_X;
                case ZeroPageIndirect _ -> ZP_INDIRECT;
                case ZeroPageIndirectIndexedY _ -> ZP_INDIRECT_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record STX(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0x8E;
        public static final byte ZP = (byte) 0x86;
        public static final byte ZP_Y = (byte) 0x96;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedY _ -> ZP_Y;
                default -> throw unsupported(this);
            });
        }
    }

    record STY(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0x8C;
        public static final byte ZP = (byte) 0x84;
        public static final byte ZP_X = (byte) 0x94;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record STZ(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0x9C;
        public static final byte ABSOLUTE_X = (byte) 0x9E;
        public static final byte ZP = 0x64;
        public static final byte ZP_X = 0x74;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case AbsoluteIndexedX _ -> ABSOLUTE_X;
                case ZeroPage _ -> ZP;
                case ZeroPageIndexedX _ -> ZP_X;
                default -> throw unsupported(this);
            });
        }
    }

    record TAX() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xAA;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record TAY() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xA8;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record TSX() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xBA;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record TXA() implements Operation
    {
        public static final byte IMPLIED = (byte) 0x8A;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record TXS() implements Operation
    {
        public static final byte IMPLIED = (byte) 0x9A;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    record TYA() implements Operation
    {
        public static final byte IMPLIED = (byte) 0x98;

        public AddressMode mode()
        {
            return implied();
        }

        public Value code()
        {
            return Value.of(IMPLIED);
        }
    }

    // @formatter:off
    static ADC adc(AddressMode mode) { return new ADC(mode); }
    static AND and(AddressMode mode) { return new AND(mode); }
    static ASL asl(AddressMode mode) { return new ASL(mode); }
    static BBR0 bbr0(ZeroPageRelative mode) { return new BBR0(mode); }
    static BBR1 bbr1(ZeroPageRelative mode) { return new BBR1(mode); }
    static BBR2 bbr2(ZeroPageRelative mode) { return new BBR2(mode); }
    static BBR3 bbr3(ZeroPageRelative mode) { return new BBR3(mode); }
    static BBR4 bbr4(ZeroPageRelative mode) { return new BBR4(mode); }
    static BBR5 bbr5(ZeroPageRelative mode) { return new BBR5(mode); }
    static BBR6 bbr6(ZeroPageRelative mode) { return new BBR6(mode); }
    static BBR7 bbr7(ZeroPageRelative mode) { return new BBR7(mode); }
    static BBS0 bbs0(ZeroPageRelative mode) { return new BBS0(mode); }
    static BBS1 bbs1(ZeroPageRelative mode) { return new BBS1(mode); }
    static BBS2 bbs2(ZeroPageRelative mode) { return new BBS2(mode); }
    static BBS3 bbs3(ZeroPageRelative mode) { return new BBS3(mode); }
    static BBS4 bbs4(ZeroPageRelative mode) { return new BBS4(mode); }
    static BBS5 bbs5(ZeroPageRelative mode) { return new BBS5(mode); }
    static BBS6 bbs6(ZeroPageRelative mode) { return new BBS6(mode); }
    static BBS7 bbs7(ZeroPageRelative mode) { return new BBS7(mode); }
    static BCC bcc(Relative mode) { return new BCC(mode); }
    static BCS bcs(Relative mode) { return new BCS(mode); }
    static BEQ beq(Relative mode) { return new BEQ(mode); }
    static BIT bit(AddressMode mode) { return new BIT(mode); }
    static BMI bmi(Relative mode) { return new BMI(mode); }
    static BNE bne(Relative mode) { return new BNE(mode); }
    static BPL bpl(Relative mode) { return new BPL(mode); }
    static BRA bra(Relative mode) { return new BRA(mode); }
    static BRK brk() { return new BRK(); }
    static BVC bvc(Relative mode) { return new BVC(mode); }
    static BVS bvs(Relative mode) { return new BVS(mode); }
    static CLC clc() { return new CLC(); }
    static CLD cld() { return new CLD(); }
    static CLI cli() { return new CLI(); }
    static CLV clv() { return new CLV(); }
    static CMP cmp(AddressMode mode) { return new CMP(mode); }
    static CPX cpx(AddressMode mode) { return new CPX(mode); }
    static CPY cpy(AddressMode mode) { return new CPY(mode); }
    static DEC dec(AddressMode mode) { return new DEC(mode); }
    static DEX dex() { return new DEX(); }
    static DEY dey() { return new DEY(); }
    static EOR eor(AddressMode mode) { return new EOR(mode); }
    static INC inc(AddressMode mode) { return new INC(mode); }
    static INX inx() { return new INX(); }
    static INY iny() { return new INY(); }
    static JMP jmp(AddressMode mode) { return new JMP(mode); }
    static JSR jsr(Absolute mode) { return new JSR(mode); }
    static LDA lda(AddressMode mode) { return new LDA(mode); }
    static LDX ldx(AddressMode mode) { return new LDX(mode); }
    static LDY ldy(AddressMode mode) { return new LDY(mode); }
    static LSR lsr(AddressMode mode) { return new LSR(mode); }
    static NOP nop() { return new NOP(); }
    static ORA ora(AddressMode mode) { return new ORA(mode); }
    static PHA pha() { return new PHA(); }
    static PHP php() { return new PHP(); }
    static PHX phx() { return new PHX(); }
    static PHY phy() { return new PHY(); }
    static PLA pla() { return new PLA(); }
    static PLP plp() { return new PLP(); }
    static PLX plx() { return new PLX(); }
    static PLY ply() { return new PLY(); }
    static RMB0 rmb0(ZeroPage mode) { return new RMB0(mode); }
    static RMB1 rmb1(ZeroPage mode) { return new RMB1(mode); }
    static RMB2 rmb2(ZeroPage mode) { return new RMB2(mode); }
    static RMB3 rmb3(ZeroPage mode) { return new RMB3(mode); }
    static RMB4 rmb4(ZeroPage mode) { return new RMB4(mode); }
    static RMB5 rmb5(ZeroPage mode) { return new RMB5(mode); }
    static RMB6 rmb6(ZeroPage mode) { return new RMB6(mode); }
    static RMB7 rmb7(ZeroPage mode) { return new RMB7(mode); }
    static ROL rol(AddressMode mode) { return new ROL(mode); }
    static ROR ror(AddressMode mode) { return new ROR(mode); }
    static RTI rti() { return new RTI(); }
    static RTS rts() { return new RTS(); }
    static SBC sbc(AddressMode mode) { return new SBC(mode); }
    static SEC sec() { return new SEC(); }
    static SED sed() { return new SED(); }
    static SEI sei() { return new SEI(); }
    static SMB0 smb0(ZeroPage mode) { return new SMB0(mode); }
    static SMB1 smb1(ZeroPage mode) { return new SMB1(mode); }
    static SMB2 smb2(ZeroPage mode) { return new SMB2(mode); }
    static SMB3 smb3(ZeroPage mode) { return new SMB3(mode); }
    static SMB4 smb4(ZeroPage mode) { return new SMB4(mode); }
    static SMB5 smb5(ZeroPage mode) { return new SMB5(mode); }
    static SMB6 smb6(ZeroPage mode) { return new SMB6(mode); }
    static SMB7 smb7(ZeroPage mode) { return new SMB7(mode); }
    static STA sta(AddressMode mode) { return new STA(mode); }
    // TODO STP
    static STX stx(AddressMode mode) { return new STX(mode); }
    static STY sty(AddressMode mode) { return new STY(mode); }
    static STZ stz(AddressMode mode) { return new STZ(mode); }
    static TAX tax() { return new TAX(); }
    static TAY tay() { return new TAY(); }
    // TODO TRB
    // TODO TSB
    static TSX tsx() { return new TSX(); }
    static TXA txa() { return new TXA(); }
    static TXS txs() { return new TXS(); }
    static TYA tya() { return new TYA(); }
    // TODO WAI
    // @formatter:on

    static List<Value> toValues(Operation operation)
    {
        return switch (operation.mode()) {
            case Absolute(Address a) -> Stream.concat(Stream.of(operation.code()), a.bytes().stream()).toList();
            case AbsoluteIndexedXIndirect(Address a) ->
                    Stream.concat(Stream.of(operation.code()), a.bytes().stream()).toList();
            case AbsoluteIndexedX(Address a) -> Stream.concat(Stream.of(operation.code()), a.bytes().stream()).toList();
            case AbsoluteIndexedY(Address a) -> Stream.concat(Stream.of(operation.code()), a.bytes().stream()).toList();
            case AbsoluteIndirect(Address a) -> Stream.concat(Stream.of(operation.code()), a.bytes().stream()).toList();
            case Accumulator _ -> List.of(operation.code());
            case Immediate(Value value) -> List.of(operation.code(), value);
            case Implied _ -> List.of(operation.code());
            case Relative(Value offset) -> List.of(operation.code(), offset);
            case AddressMode.Stack _ -> List.of(operation.code());
            case ZeroPage(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndexedXIndirect(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndexedX(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndexedY(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndirect(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndirectIndexedY(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageRelative(ZeroPage zp, Relative relative) ->
                    List.of(operation.code(), zp.offset(), relative.displacement());
        };
    }

    private static UnsupportedOperationException unsupported(Operation op)
    {
        throw new UnsupportedOperationException("Unsupported " +
                                                op.getClass().getSimpleName() +
                                                " addressing mode: " +
                                                op.mode());
    }
}
