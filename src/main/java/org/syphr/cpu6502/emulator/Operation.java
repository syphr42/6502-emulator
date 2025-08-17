package org.syphr.cpu6502.emulator;

public sealed interface Operation
{
    record DEC() implements Operation {}

    record INC() implements Operation {}

    record LDA(Expression expression) implements Operation {}

    record PHA() implements Operation {}

    record PLA() implements Operation {}

    record STA(Address address) implements Operation {}
}
