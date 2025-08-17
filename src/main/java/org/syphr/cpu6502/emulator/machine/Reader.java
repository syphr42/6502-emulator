package org.syphr.cpu6502.emulator.machine;

public interface Reader
{
    Value read(Address address);
}
