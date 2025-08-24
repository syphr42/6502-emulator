package org.syphr.cpu6502.emulator.machine;

import java.util.List;
import java.util.stream.Stream;

import static org.syphr.cpu6502.emulator.machine.AddressMode.*;

public sealed interface Operation
{
    static List<Value> toValues(Operation operation)
    {
        return switch (operation) {
            case Operation.ADC(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) ->
                        Stream.concat(Stream.of(Value.of(ADC.ABSOLUTE)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(ADC.IMMEDIATE), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.AND(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) ->
                        Stream.concat(Stream.of(Value.of(AND.ABSOLUTE)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(AND.IMMEDIATE), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.ASL(AddressMode mode) -> switch (mode) {
                case Accumulator _ -> List.of(Value.of(ASL.ACCUMULATOR));
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BCC(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(BCC.RELATIVE), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BCS(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(BCS.RELATIVE), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BEQ(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(BEQ.RELATIVE), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BIT(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) ->
                        Stream.concat(Stream.of(Value.of(BIT.ABSOLUTE)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(BIT.IMMEDIATE), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BMI(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(BMI.RELATIVE), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BNE(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(BNE.RELATIVE), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BPL(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(BPL.RELATIVE), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BRA(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(BRA.RELATIVE), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.DEC(AddressMode mode) -> switch (mode) {
                case Accumulator _ -> List.of(Value.of(DEC.ACCUMULATOR));
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.INC(AddressMode mode) -> switch (mode) {
                case Accumulator _ -> List.of(Value.of(INC.ACCUMULATOR));
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.JMP(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) ->
                        Stream.concat(Stream.of(Value.of(JMP.ABSOLUTE)), a.bytes().stream()).toList();
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.JSR(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) ->
                        Stream.concat(Stream.of(Value.of(JSR.ABSOLUTE)), a.bytes().stream()).toList();
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.LDA(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) ->
                        Stream.concat(Stream.of(Value.of(LDA.ABSOLUTE)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(LDA.IMMEDIATE), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.NOP _ -> List.of(Value.of(NOP.IMPLIED));
            case Operation.ORA(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) ->
                        Stream.concat(Stream.of(Value.of(ORA.ABSOLUTE)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(ORA.IMMEDIATE), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.PHA _ -> List.of(Value.of(PHA.STACK));
            case Operation.PLA _ -> List.of(Value.of(PLA.STACK));
            case Operation.RTS _ -> List.of(Value.of(RTS.STACK));
            case Operation.STA(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) ->
                        Stream.concat(Stream.of(Value.of(STA.ABSOLUTE)), a.bytes().stream()).toList();
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
        };
    }

    // @formatter:off
    record ADC(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x6D;
        public static final byte IMMEDIATE = 0x69;
    }
    static ADC adc(AddressMode mode) { return new ADC(mode); }

    record AND(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x2D;
        public static final byte IMMEDIATE = 0x29;
    }
    static AND and(AddressMode mode) { return new AND(mode); }

    record ASL(AddressMode mode) implements Operation
    {
        public static final byte ACCUMULATOR = 0x0A;
    }
    static ASL asl(AddressMode mode) { return new ASL(mode); }

    record BCC(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x90;
    }
    static BCC bcc(AddressMode mode) { return new BCC(mode); }

    record BCS(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xB0;
    }
    static BCS bcs(AddressMode mode) { return new BCS(mode); }

    record BEQ(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xF0;
    }
    static BEQ beq(AddressMode mode) { return new BEQ(mode); }

    record BIT(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x2C;
        public static final byte IMMEDIATE = (byte) 0x89;
    }
    static BIT bit(AddressMode mode) { return new BIT(mode); }

    record BMI(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = 0x30;
    }
    static BMI bmi(AddressMode mode) { return new BMI(mode); }

    record BNE(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0xD0;
    }
    static BNE bne(AddressMode mode) { return new BNE(mode); }

    record BPL(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = 0x10;
    }
    static BPL bpl(AddressMode mode) { return new BPL(mode); }

    record BRA(AddressMode mode) implements Operation
    {
        public static final byte RELATIVE = (byte) 0x80;
    }
    static BRA bra(AddressMode mode) { return new BRA(mode); }

    record DEC(AddressMode mode) implements Operation
    {
        public static final byte ACCUMULATOR = 0x3A;
    }
    static DEC dec(AddressMode mode) { return new DEC(mode); }

    record INC(AddressMode mode) implements Operation
    {
        public static final byte ACCUMULATOR = 0x1A;
    }
    static INC inc(AddressMode mode) { return new INC(mode); }

    record JMP(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x4C;
    }
    static JMP jmp(AddressMode mode) { return new JMP(mode); }

    record JSR(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x20;
    }
    static JSR jsr(AddressMode mode) { return new JSR(mode); }

    record LDA(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0xAD;
        public static final byte IMMEDIATE = (byte) 0xA9;
    }
    static LDA lda(AddressMode mode) { return new LDA(mode); }

    record NOP() implements Operation
    {
        public static final byte IMPLIED = (byte) 0xEA;
    }
    static NOP nop() { return new NOP(); }

    record ORA(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = 0x0D;
        public static final byte IMMEDIATE = 0x09;
    }
    static ORA ora(AddressMode mode) { return new ORA(mode); }

    record PHA() implements Operation
    {
        public static final byte STACK = 0x48;
    }
    static PHA pha() { return new PHA(); }

    record PLA() implements Operation
    {
        public static final byte STACK = 0x68;
    }
    static PLA pla() { return new PLA(); }

    record RTS() implements Operation
    {
        public static final byte STACK = 0x60;
    }
    static RTS rts() { return new RTS(); }

    record STA(AddressMode mode) implements Operation
    {
        public static final byte ABSOLUTE = (byte) 0x8D;
    }
    static STA sta(AddressMode mode) { return new STA(mode); }
    // @formatter:on
}
