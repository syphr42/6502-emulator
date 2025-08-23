package org.syphr.cpu6502.emulator.machine;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.BitSet;

@Slf4j
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Register
{
    @ToString.Include
    private Value value = Value.ZERO;

    public void decrement()
    {
        value = value.decrement();
    }

    public void increment()
    {
        value = value.increment();
    }

    public boolean isNegative()
    {
        return value.data() < 0;
    }

    public boolean isZero()
    {
        return value.data() == 0;
    }

    public void store(Value value)
    {
        this.value = value;
    }

    public Value value()
    {
        return value;
    }

    public BitSet toBits()
    {
        return BitSet.valueOf(new byte[] {value.data()});
    }
}
