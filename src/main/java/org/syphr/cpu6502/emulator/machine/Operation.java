package org.syphr.cpu6502.emulator.machine;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public sealed interface Operation
{
    static Operation next(Iterator<Value> program)
    {
        if (!program.hasNext()) {
            throw new IllegalStateException("Program has no operations left!");
        }

        Value opCode = program.next();
        return switch (opCode.data()) {
            case 0x09 -> ora(program.next());
            case 0x0A -> asl();
            case 0x0D -> ora(Address.of(program.next(), program.next()));
            case 0x1A -> inc();
            case 0x20 -> jsr(Address.of(program.next(), program.next()));
            case 0x29 -> and(program.next());
            case 0x2D -> and(Address.of(program.next(), program.next()));
            case 0x3A -> dec();
            case 0x48 -> pha();
            case 0x4C -> jmp(Address.of(program.next(), program.next()));
            case 0x60 -> rts();
            case 0x68 -> pla();
            case 0x69 -> adc(program.next());
            case 0x6D -> adc(Address.of(program.next(), program.next()));
            case (byte) 0x8D -> sta(Address.of(program.next(), program.next()));
            case (byte) 0x90 -> bcc(program.next());
            case (byte) 0xA9 -> lda(program.next());
            case (byte) 0xAD -> lda(Address.of(program.next(), program.next()));
            case (byte) 0xEA -> nop();
            default -> throw new UnsupportedOperationException("Unsupported op code: " + opCode);
        };
    }

    static List<Value> toValues(Operation operation)
    {
        return switch (operation) {
            case Operation.ADC(Expression e) -> switch (e) {
                case Address a -> Stream.concat(Stream.of(Value.of(0x6D)), a.bytes().stream()).toList();
                case Value v -> List.of(Value.of(0x69), v);
            };
            case Operation.AND(Expression e) -> switch (e) {
                case Address a -> Stream.concat(Stream.of(Value.of(0x2D)), a.bytes().stream()).toList();
                case Value v -> List.of(Value.of(0x29), v);
            };
            case Operation.ASL _ -> List.of(Value.of(0x0A));
            case Operation.BCC(Value v) -> List.of(Value.of(0x90), v);
            case Operation.DEC _ -> List.of(Value.of(0x3A));
            case Operation.INC _ -> List.of(Value.of(0x1A));
            case Operation.JMP(Address a) -> Stream.concat(Stream.of(Value.of(0x4C)), a.bytes().stream()).toList();
            case Operation.JSR(Address a) -> Stream.concat(Stream.of(Value.of(0x20)), a.bytes().stream()).toList();
            case Operation.LDA(Expression e) -> switch (e) {
                case Address a -> Stream.concat(Stream.of(Value.of(0xAD)), a.bytes().stream()).toList();
                case Value v -> List.of(Value.of(0xA9), v);
            };
            case Operation.NOP _ -> List.of(Value.of(0xEA));
            case Operation.ORA(Expression e) -> switch (e) {
                case Address a -> Stream.concat(Stream.of(Value.of(0x0D)), a.bytes().stream()).toList();
                case Value v -> List.of(Value.of(0x09), v);
            };
            case Operation.PHA _ -> List.of(Value.of(0x48));
            case Operation.PLA _ -> List.of(Value.of(0x68));
            case Operation.RTS _ -> List.of(Value.of(0x60));
            case Operation.STA(Address a) -> Stream.concat(Stream.of(Value.of(0x8D)), a.bytes().stream()).toList();
        };
    }

    // @formatter:off
    record ADC(Expression expression) implements Operation {}
    static ADC adc(Expression expression) { return new ADC(expression); }

    record AND(Expression expression) implements Operation {}
    static AND and(Expression expression) { return new AND(expression); }

    record ASL() implements Operation {}
    static ASL asl() { return new ASL(); }

    record BCC(Value displacement) implements Operation {}
    static BCC bcc(Value displacement) { return new BCC(displacement); }

    record DEC() implements Operation {}
    static DEC dec() { return new DEC(); }

    record INC() implements Operation {}
    static INC inc() { return new INC(); }

    record JMP(Address address) implements Operation {}
    static JMP jmp(Address address) { return new JMP(address); }

    record JSR(Address address) implements Operation {}
    static JSR jsr(Address address) { return new JSR(address); }

    record LDA(Expression expression) implements Operation {}
    static LDA lda(Expression expression) { return new LDA(expression); }

    record NOP() implements Operation {}
    static NOP nop() { return new NOP(); }

    record ORA(Expression expression) implements Operation {}
    static ORA ora(Expression expression) { return new ORA(expression); }

    record PHA() implements Operation {}
    static PHA pha() { return new PHA(); }

    record PLA() implements Operation {}
    static PLA pla() { return new PLA(); }

    record RTS() implements Operation {}
    static RTS rts() { return new RTS(); }

    record STA(Address address) implements Operation {}
    static STA sta(Address address) { return new STA(address); }
    // @formatter:on
}
