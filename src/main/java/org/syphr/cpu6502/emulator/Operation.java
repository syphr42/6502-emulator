package org.syphr.cpu6502.emulator;

public sealed interface Operation
{
    record DEC() implements Operation {}

    record INC() implements Operation {}

    record LDA(Value value) implements Operation {}
}
