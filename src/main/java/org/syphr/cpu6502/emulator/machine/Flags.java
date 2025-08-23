package org.syphr.cpu6502.emulator.machine;

import lombok.Builder;

import java.util.BitSet;

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
    public static Flags of(BitSet bits)
    {
        if (bits.length() != 8) {
            throw new IllegalArgumentException("Processor status flags bit set must have 8 bits");
        }

        return Flags.builder()
                    .negative(bits.get(0))
                    .overflow(bits.get(1))
                    .user(bits.get(2))
                    .breakCommand(bits.get(3))
                    .decimal(bits.get(4))
                    .irqDisable(bits.get(5))
                    .zero(bits.get(6))
                    .carry(bits.get(7))
                    .build();
    }

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
