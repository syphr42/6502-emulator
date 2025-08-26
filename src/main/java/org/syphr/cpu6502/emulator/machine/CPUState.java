package org.syphr.cpu6502.emulator.machine;

// TODO capture stack
public record CPUState(Value accumulator, Value x, Value y, Flags flags, Address programCounter)
{}
