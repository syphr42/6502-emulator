package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.BitSet;

import static org.assertj.core.api.Assertions.assertThat;

class FlagsTest
{
    @ParameterizedTest
    @CsvSource({"11111111, true, true, true, true, true, true, true, true",
                "01111111, false, true, true, true, true, true, true, true",
                "00111111, false, false, true, true, true, true, true, true",
                "00011111, false, false, false, true, true, true, true, true",
                "00001111, false, false, false, false, true, true, true, true",
                "00000111, false, false, false, false, false, true, true, true",
                "00000011, false, false, false, false, false, false, true, true",
                "00000001, false, false, false, false, false, false, false, true",
                "00000000, false, false, false, false, false, false, false, false"})
    void of_BitSet(String b,
                   boolean isNegative,
                   boolean isOverflow,
                   boolean isUser,
                   boolean isBreakCommand,
                   boolean isDecimal,
                   boolean isIrqDisable,
                   boolean isZero,
                   boolean isCarry)
    {
        // given
        var bits = BitSet.valueOf(new byte[] {(byte) Integer.parseInt(b, 2)});

        // when
        var result = Flags.of(bits);

        // then
        assertThat(result).isEqualTo(new Flags(isNegative,
                                               isOverflow,
                                               isUser,
                                               isBreakCommand,
                                               isDecimal,
                                               isIrqDisable,
                                               isZero,
                                               isCarry));
    }

    @ParameterizedTest
    @CsvSource({"11111111, true, true, true, true, true, true, true, true",
                "01111111, false, true, true, true, true, true, true, true",
                "00111111, false, false, true, true, true, true, true, true",
                "00011111, false, false, false, true, true, true, true, true",
                "00001111, false, false, false, false, true, true, true, true",
                "00000111, false, false, false, false, false, true, true, true",
                "00000011, false, false, false, false, false, false, true, true",
                "00000001, false, false, false, false, false, false, false, true",
                "00000000, false, false, false, false, false, false, false, false"})
    void asByte(String b,
                boolean isNegative,
                boolean isOverflow,
                boolean isUser,
                boolean isBreakCommand,
                boolean isDecimal,
                boolean isIrqDisable,
                boolean isZero,
                boolean isCarry)
    {
        // given
        var flags = new Flags(isNegative,
                              isOverflow,
                              isUser,
                              isBreakCommand,
                              isDecimal,
                              isIrqDisable,
                              isZero,
                              isCarry);

        // when
        byte result = flags.asByte();

        // then
        assertThat(result).isEqualTo((byte) Integer.parseInt(b, 2));
    }
}
