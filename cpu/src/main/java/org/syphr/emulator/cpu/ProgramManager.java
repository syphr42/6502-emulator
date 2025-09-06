package org.syphr.emulator.cpu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ProgramManager
{
    private final Reader reader;

    @Getter
    private Address programCounter = Address.of(0x00FF);

    public Value nextValue()
    {
        Value value = reader.read(programCounter);
        programCounter = programCounter.increment();

        return value;
    }

    public Address nextAddress()
    {
        return Address.of(nextValue(), nextValue());
    }

    public void setProgramCounter(Address address)
    {
        programCounter = address;
        log.info("Program counter set to {}", address);
    }
}
