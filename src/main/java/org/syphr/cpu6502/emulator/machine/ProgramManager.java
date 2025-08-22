package org.syphr.cpu6502.emulator.machine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
class ProgramManager implements Iterator<Value>
{
    private static final Address RESET_ADDRESS = Address.of(0xFFFC);

    private final Clock clock;
    private final Reader reader;

    @Getter
    private Address programCounter = RESET_ADDRESS;

    @Override
    public Value next()
    {
        clock.nextCycle();

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
        clock.nextCycle();
        log.info("Jump to {}", address);
        programCounter = address;
    }

    public void reset()
    {
        programCounter = RESET_ADDRESS;
    }
}
