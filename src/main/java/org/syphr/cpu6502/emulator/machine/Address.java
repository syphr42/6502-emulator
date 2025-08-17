package org.syphr.cpu6502.emulator.machine;

public record Address(Value high, Value low) implements Expression
{}
