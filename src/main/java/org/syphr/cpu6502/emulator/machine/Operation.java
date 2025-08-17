package org.syphr.cpu6502.emulator.machine;

public sealed interface Operation
{
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
