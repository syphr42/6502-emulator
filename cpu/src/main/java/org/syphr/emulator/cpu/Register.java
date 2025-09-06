package org.syphr.emulator.cpu;

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
        return value.isNegative();
    }

    public boolean isZero()
    {
        return value.isZero();
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
