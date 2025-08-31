package org.syphr.cpu6502.emulator.machine;

import java.util.List;

public record Address(short data) implements Comparable<Address>
{
    public static final Address ZERO = Address.of(0);
    public static final Address RESET = Address.of(0xFFFC);

    public static Address of(short s)
    {
        return new Address(s);
    }

    public static Address of(int i)
    {
        return new Address((short) i);
    }

    public static Address ofHex(String hex)
    {
        return new Address((short) Integer.parseInt(hex, 16));
    }

    public static Address ofBits(String bits)
    {
        return new Address((short) Integer.parseInt(bits, 2));
    }

    public static Address of(Value low, Value high)
    {
        return Address.of((short) ((high.data() << 8) | (low.data() & 0xFF)));
    }

    public Address increment()
    {
        return Address.of(data + 1);
    }

    public Address decrement()
    {
        return Address.of(data - 1);
    }

    public Address plus(Value v)
    {
        return Address.of(data + v.data());
    }

    public Value low()
    {
        return Value.of(data);
    }

    public Value high()
    {
        return Value.of(data >> 8);
    }

    public List<Value> bytes()
    {
        return List.of(low(), high());
    }

    @Override
    public int compareTo(Address o)
    {
        return Short.compareUnsigned(data, o.data);
    }

    @Override
    public String toString()
    {
        return Address.class.getSimpleName() + "[0x%04X]".formatted(data);
    }
}
