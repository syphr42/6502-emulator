package org.syphr.cpu6502.emulator;

public record Address(Value high, Value low) implements Expression
{}
