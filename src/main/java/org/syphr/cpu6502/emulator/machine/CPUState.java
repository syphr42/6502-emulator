package org.syphr.cpu6502.emulator.machine;

import java.util.List;

public record CPUState(Address programCounter,
                       Value accumulator,
                       Value x,
                       Value y,
                       Address stackPointer,
                       List<Value> stackData,
                       Flags flags)
{}
