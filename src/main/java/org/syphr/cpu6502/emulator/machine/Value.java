package org.syphr.cpu6502.emulator.machine;

import java.util.HexFormat;

public record Value(byte data) implements Expression
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

    @Override
    public String toString()
    {
        return Value.class.getSimpleName() + "[" + "0x" + HexFormat.of().toHexDigits(data) + ']';
    }
}
