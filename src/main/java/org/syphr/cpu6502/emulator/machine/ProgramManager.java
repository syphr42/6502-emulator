package org.syphr.cpu6502.emulator.machine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
class ProgramManager implements Iterator<Value>
{
    private final Reader reader;

    @Getter
    private Address programCounter = Address.of(0x00FF);

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

    public void setProgramCounter(Address address)
    {
        programCounter = address;
        log.info("Program counter set to {}", address);
    }
}
