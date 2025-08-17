package org.syphr.cpu6502.emulator.machine;

public interface Writer
{
    void write(Address address, Value value);
}
