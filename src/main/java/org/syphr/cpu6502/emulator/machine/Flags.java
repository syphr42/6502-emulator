package org.syphr.cpu6502.emulator.machine;

import lombok.Builder;

@Builder(toBuilder = true)
public record Flags(boolean negative,
                    boolean overflow,
                    boolean user,
                    boolean breakCommand,
                    boolean decimal,
                    boolean irqDisable,
                    boolean zero,
                    boolean carry)
{
    public byte negativeBit()
    {
        return bit(negative);
    }

    public byte overflowBit()
    {
        return bit(overflow);
    }

    public byte userBit()
    {
        return bit(user);
    }

    public byte breakCommandBit()
    {
        return bit(breakCommand);
    }

    public byte decimalBit()
    {
        return bit(decimal);
    }

    public byte irqDisableBit()
    {
        return bit(irqDisable);
    }

    public byte zeroBit()
    {
        return bit(zero);
    }

    public byte carryBit()
    {
        return bit(carry);
    }

    private byte bit(boolean b)
    {
        return (byte) (b ? 1 : 0);
    }
}
