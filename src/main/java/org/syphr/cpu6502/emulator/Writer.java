package org.syphr.cpu6502.emulator;

public interface Writer
{
    void write(Address address, byte data);
}
