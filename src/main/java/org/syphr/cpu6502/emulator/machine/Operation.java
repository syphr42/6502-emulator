package org.syphr.cpu6502.emulator.machine;

import java.util.List;
import java.util.stream.Stream;

import static org.syphr.cpu6502.emulator.machine.AddressMode.*;

public sealed interface Operation
{
    Value code();

    AddressMode mode();

    record ADC(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x6D;
        public static final byte IMMEDIATE = 0x69;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
                default -> throw unsupported(this);
            });
        }
    }

    record AND(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x2D;
        public static final byte IMMEDIATE = 0x29;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
                default -> throw unsupported(this);
            });
        }
    }

    record ASL(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x0E;
        public static final byte ACCUMULATOR = 0x0A;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Accumulator _ -> ACCUMULATOR;
                default -> throw unsupported(this);
            });
        }
    }

    record BCC(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x90;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
        }
    }

    record BCS(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xB0;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
        }
    }

    record BEQ(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xF0;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
        }
    }

    record BIT(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x2C;
        public static final byte IMMEDIATE = (byte) 0x89;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
                default -> throw unsupported(this);
            });
        }
    }

    record BMI(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = 0x30;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
        }
    }

    record BNE(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xD0;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
        }
    }

    record BPL(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = 0x10;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
        }
    }

    record BRA(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x80;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
        }
    }

    record BVC(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x50;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
        }
    }

    record BVS(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x70;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Relative _ -> RELATIVE;
                default -> throw unsupported(this);
            });
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
        public static final byte IMMEDIATE = (byte) 0xC9;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
                default -> throw unsupported(this);
            });
        }
    }

    record CPX(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xEC;
        public static final byte IMMEDIATE = (byte) 0xE0;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
                default -> throw unsupported(this);
            });
        }
    }

    record CPY(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xCC;
        public static final byte IMMEDIATE = (byte) 0xC0;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
                default -> throw unsupported(this);
            });
        }
    }

    record DEC(AddressMode mode) implements Operation
    {
        public static final byte ACCUMULATOR = 0x3A;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Accumulator _ -> ACCUMULATOR;
                default -> throw unsupported(this);
            });
        }
    }

    record INC(AddressMode mode) implements Operation
    {
        public static final byte ACCUMULATOR = 0x1A;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Accumulator _ -> ACCUMULATOR;
                default -> throw unsupported(this);
            });
        }
    }

    record JMP(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x4C;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                default -> throw unsupported(this);
            });
        }
    }

    record JSR(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x20;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                default -> throw unsupported(this);
            });
        }
    }

    record LDA(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xAD;
        public static final byte IMMEDIATE = (byte) 0xA9;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
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
        public static final byte IMMEDIATE = 0x09;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                case Immediate _ -> IMMEDIATE;
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

    record STA(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0x8D;

        public Value code()
        {
            return Value.of(switch (mode) {
                case Absolute _ -> ABSOLUTE;
                default -> throw unsupported(this);
            });
        }
    }

    // @formatter:off
    static ADC adc(AddressMode mode) { return new ADC(mode); }
    static AND and(AddressMode mode) { return new AND(mode); }
    static ASL asl(AddressMode mode) { return new ASL(mode); }
    static BCC bcc(AddressMode mode) { return new BCC(mode); }
    static BCS bcs(AddressMode mode) { return new BCS(mode); }
    static BEQ beq(AddressMode mode) { return new BEQ(mode); }
    static BIT bit(AddressMode mode) { return new BIT(mode); }
    static BMI bmi(AddressMode mode) { return new BMI(mode); }
    static BNE bne(AddressMode mode) { return new BNE(mode); }
    static BPL bpl(AddressMode mode) { return new BPL(mode); }
    static BRA bra(AddressMode mode) { return new BRA(mode); }
    static BVC bvc(AddressMode mode) { return new BVC(mode); }
    static BVS bvs(AddressMode mode) { return new BVS(mode); }
    static CLC clc() { return new CLC(); }
    static CLD cld() { return new CLD(); }
    static CLI cli() { return new CLI(); }
    static CLV clv() { return new CLV(); }
    static CMP cmp(AddressMode mode) { return new CMP(mode); }
    static CPX cpx(AddressMode mode) { return new CPX(mode); }
    static CPY cpy(AddressMode mode) { return new CPY(mode); }
    static DEC dec(AddressMode mode) { return new DEC(mode); }
    static INC inc(AddressMode mode) { return new INC(mode); }
    static JMP jmp(AddressMode mode) { return new JMP(mode); }
    static LDA lda(AddressMode mode) { return new LDA(mode); }
    static JSR jsr(AddressMode mode) { return new JSR(mode); }
    static NOP nop() { return new NOP(); }
    static ORA ora(AddressMode mode) { return new ORA(mode); }
    static PHA pha() { return new PHA(); }
    static PLA pla() { return new PLA(); }
    static RTS rts() { return new RTS(); }
    static STA sta(AddressMode mode) { return new STA(mode); }
    // @formatter:on

    static List<Value> toValues(Operation operation)
    {
        return switch (operation.mode()) {
            case Absolute(Address a) -> Stream.concat(Stream.of(operation.code()), a.bytes().stream()).toList();
            case AbsoluteIndexedIndirectX(Address a) ->
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
            case ZeroPageIndexedIndirectX(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndexedX(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndexedY(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndirect(Value offset) -> List.of(operation.code(), offset);
            case ZeroPageIndirectIndexedY(Value offset) -> List.of(operation.code(), offset);
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
