package org.syphr.cpu6502.emulator.machine;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;

@RequiredArgsConstructor
public class ProgramManager implements Iterator<Value>
{
    private static final Address RESET_ADDRESS = Address.ofHex("FFFC");

    private final Reader reader;

    private Address programCounter = RESET_ADDRESS;

    @Override
    public Value next()
    {
        Value value = reader.read(programCounter);
        programCounter = programCounter.increment();

        return value;
    }

    @Override
    public boolean hasNext()
    {
        return true;
    }

    public void jump(Address address)
    {
        programCounter = address;
    }

    public void reset()
    {
        programCounter = RESET_ADDRESS;
    }
}
