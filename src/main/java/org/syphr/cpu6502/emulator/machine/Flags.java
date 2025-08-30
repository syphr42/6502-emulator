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
        if (bits.length() > 8) {
            throw new IllegalArgumentException("Processor status flags bit set must have 8 bits");
        }

        return Flags.builder()
                    .negative(bits.get(7))
                    .overflow(bits.get(6))
                    .user(bits.get(5))
                    .breakCommand(bits.get(4))
                    .decimal(bits.get(3))
                    .irqDisable(bits.get(2))
                    .zero(bits.get(1))
                    .carry(bits.get(0))
                    .build();
    }

    public byte asByte()
    {
        byte b = 0;
        b |= negative ? (byte) 0b10000000 : 0;
        b |= overflow ? (byte) 0b01000000 : 0;
        b |= user ? (byte) 0b00100000 : 0;
        b |= breakCommand ? (byte) 0b00010000 : 0;
        b |= decimal ? (byte) 0b00001000 : 0;
        b |= irqDisable ? (byte) 0b00000100 : 0;
        b |= zero ? (byte) 0b00000010 : 0;
        b |= carry ? (byte) 0b00000001 : 0;

        return b;
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
