package org.syphr.cpu6502.emulator.machine;

import java.util.List;
import java.util.stream.Stream;

public sealed interface Operation
{
    static Operation next(ProgramManager programManager)
    {
        return switch (programManager.next().data()) {
            case 9 -> ora(programManager.next()); // 09
            case 13 -> ora(Address.of(programManager.next(), programManager.next())); // 0D
            case 26 -> inc(); // 1A
            case 41 -> and(programManager.next()); // 29
            case 45 -> and(Address.of(programManager.next(), programManager.next())); // 2D
            case 58 -> dec(); // 3A
            case 72 -> pha(); // 48
            case 104 -> pla(); // 68
            case -115 -> sta(Address.of(programManager.next(), programManager.next())); // 8D
            case -87 -> lda(programManager.next()); // A9
            case -83 -> lda(Address.of(programManager.next(), programManager.next())); // AD
            case -22 -> nop(); // EA
            default -> nop();
        };
    }

    static List<Value> toValues(Operation operation)
    {
        return switch (operation) {
            case Operation.AND and -> switch (and.expression()) {
                case Address a -> Stream.concat(Stream.of(Value.ofHex("2D")), a.values().stream()).toList();
                case Value v -> List.of(Value.ofHex("29"), v);
            };
            case Operation.DEC _ -> List.of(Value.ofHex("3A"));
            case Operation.INC _ -> List.of(Value.ofHex("1A"));
            case Operation.LDA lda -> switch (lda.expression()) {
                case Address a -> Stream.concat(Stream.of(Value.ofHex("AD")), a.values().stream()).toList();
                case Value v -> List.of(Value.ofHex("A9"), v);
            };
            case Operation.NOP _ -> List.of(Value.ofHex("EA"));
            case Operation.ORA ora -> switch (ora.expression()) {
                case Address a -> Stream.concat(Stream.of(Value.ofHex("0D")), a.values().stream()).toList();
                case Value v -> List.of(Value.ofHex("09"), v);
            };
            case Operation.PHA _ -> List.of(Value.ofHex("48"));
            case Operation.PLA _ -> List.of(Value.ofHex("68"));
            case Operation.STA sta ->
                    Stream.concat(Stream.of(Value.ofHex("8D")), sta.address().values().stream()).toList();
        };
    }

    record AND(Expression expression) implements Operation {}

    static AND and(Expression expression)
    {
        return new AND(expression);
    }

    record DEC() implements Operation {}

    static DEC dec()
    {
        return new DEC();
    }

    record INC() implements Operation {}

    static INC inc()
    {
        return new INC();
    }

    record LDA(Expression expression) implements Operation {}

    static LDA lda(Expression expression)
    {
        return new LDA(expression);
    }

    record NOP() implements Operation {}

    static NOP nop()
    {
        return new NOP();
    }

    record ORA(Expression expression) implements Operation {}

    static ORA ora(Expression expression)
    {
        return new ORA(expression);
    }

    record PHA() implements Operation {}

    static PHA pha()
    {
        return new PHA();
    }

    record PLA() implements Operation {}

    static PLA pla()
    {
        return new PLA();
    }

    record STA(Address address) implements Operation {}

    static STA sta(Address address)
    {
        return new STA(address);
    }
}
