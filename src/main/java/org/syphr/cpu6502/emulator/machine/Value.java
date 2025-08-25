package org.syphr.cpu6502.emulator.machine;

public record Value(byte data)
{
    public static Value ZERO = Value.of(0);

    public static Value of(byte b)
    {
        return new Value(b);
    }

    public static Value of(int i)
    {
        return new Value((byte) i);
    }

    public static Value ofHex(String hex)
    {
        return new Value((byte) Integer.parseInt(hex, 16));
    }

    public static Value ofBits(String bits)
    {
        return new Value((byte) Integer.parseInt(bits, 2));
    }

    public Value and(Value other)
    {
        return Value.of(data & other.data);
    }

    public Value or(Value other)
    {
        return Value.of(data | other.data);
    }

    public Value increment()
    {
        return Value.of(data + 1);
    }

    public Value decrement()
    {
        return Value.of(data - 1);
    }

    public boolean isNegative()
    {
        return data < 0;
    }

    public boolean isZero()
    {
        return data == 0;
    }

    @Override
    public String toString()
    {
        return Value.class.getSimpleName() + "[0x%02X]".formatted(data);
    }
}
