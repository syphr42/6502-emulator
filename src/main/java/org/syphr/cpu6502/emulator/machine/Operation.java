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
                case Absolute(Address a) -> Stream.concat(Stream.of(Value.of(0x6D)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(0x69), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.AND(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) -> Stream.concat(Stream.of(Value.of(0x2D)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(0x29), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.ASL(AddressMode mode) -> switch (mode) {
                case Accumulator _ -> List.of(Value.of(0x0A));
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BCC(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(0x90), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BCS(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(0xB0), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BEQ(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(0xF0), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BIT(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) -> Stream.concat(Stream.of(Value.of(0x2C)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(0x89), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.BMI(AddressMode mode) -> switch (mode) {
                case Relative(Value offset) -> List.of(Value.of(0x30), offset);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.DEC(AddressMode mode) -> switch (mode) {
                case Accumulator _ -> List.of(Value.of(0x3A));
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.INC(AddressMode mode) -> switch (mode) {
                case Accumulator _ -> List.of(Value.of(0x1A));
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.JMP(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) -> Stream.concat(Stream.of(Value.of(0x4C)), a.bytes().stream()).toList();
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.JSR(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) -> Stream.concat(Stream.of(Value.of(0x20)), a.bytes().stream()).toList();
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.LDA(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) -> Stream.concat(Stream.of(Value.of(0xAD)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(0xA9), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.NOP _ -> List.of(Value.of(0xEA));
            case Operation.ORA(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) -> Stream.concat(Stream.of(Value.of(0x0D)), a.bytes().stream()).toList();
                case Immediate(Value v) -> List.of(Value.of(0x09), v);
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
            case Operation.PHA _ -> List.of(Value.of(0x48));
            case Operation.PLA _ -> List.of(Value.of(0x68));
            case Operation.RTS _ -> List.of(Value.of(0x60));
            case Operation.STA(AddressMode mode) -> switch (mode) {
                case Absolute(Address a) -> Stream.concat(Stream.of(Value.of(0x8D)), a.bytes().stream()).toList();
                default -> throw new UnsupportedOperationException("Unsupported operation: " + operation);
            };
        };
    }

    // @formatter:off
    record ADC(AddressMode mode) implements Operation {}
    static ADC adc(AddressMode mode) { return new ADC(mode); }

    record AND(AddressMode mode) implements Operation {}
    static AND and(AddressMode mode) { return new AND(mode); }

    record ASL(AddressMode mode) implements Operation {}
    static ASL asl(AddressMode mode) { return new ASL(mode); }

    record BCC(AddressMode mode) implements Operation {}
    static BCC bcc(AddressMode mode) { return new BCC(mode); }

    record BCS(AddressMode mode) implements Operation {}
    static BCS bcs(AddressMode mode) { return new BCS(mode); }

    record BEQ(AddressMode mode) implements Operation {}
    static BEQ beq(AddressMode mode) { return new BEQ(mode); }

    record BIT(AddressMode mode) implements Operation {}
    static BIT bit(AddressMode mode) { return new BIT(mode); }

    record BMI(AddressMode mode) implements Operation {}
    static BMI bmi(AddressMode mode) { return new BMI(mode); }

    record DEC(AddressMode mode) implements Operation {}
    static DEC dec(AddressMode mode) { return new DEC(mode); }

    record INC(AddressMode mode) implements Operation {}
    static INC inc(AddressMode mode) { return new INC(mode); }

    record JMP(AddressMode mode) implements Operation {}
    static JMP jmp(AddressMode mode) { return new JMP(mode); }

    record JSR(AddressMode mode) implements Operation {}
    static JSR jsr(AddressMode mode) { return new JSR(mode); }

    record LDA(AddressMode mode) implements Operation {}
    static LDA lda(AddressMode mode) { return new LDA(mode); }

    record NOP() implements Operation {}
    static NOP nop() { return new NOP(); }

    record ORA(AddressMode mode) implements Operation {}
    static ORA ora(AddressMode mode) { return new ORA(mode); }

    record PHA() implements Operation {}
    static PHA pha() { return new PHA(); }

    record PLA() implements Operation {}
    static PLA pla() { return new PLA(); }

    record RTS() implements Operation {}
    static RTS rts() { return new RTS(); }

    record STA(AddressMode mode) implements Operation {}
    static STA sta(AddressMode mode) { return new STA(mode); }
    // @formatter:on
}
