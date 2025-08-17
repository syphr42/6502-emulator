package org.syphr.cpu6502.emulator.machine;

import java.util.HexFormat;

public record Address(short data) implements Expression
{
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

    @Override
    public String toString()
    {
        return Address.class.getSimpleName() + "[" + "0x" + HexFormat.of().toHexDigits(data) + ']';
    }
}
