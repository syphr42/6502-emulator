package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValueTest
{
    @Test
    void zero_IsZero()
    {
        assertThat(Value.ZERO).isEqualTo(Value.of(0));
    }

    @Test
    void of_Byte()
    {
        // given
        byte b = 42;

        // when
        var result = Value.of(b);

        // then
        assertThat(result.data()).isEqualTo((byte) 42);
    }

    @Test
    void of_Int()
    {
        // given
        int i = 42;

        // when
        var result = Value.of(i);

        // then
        assertThat(result.data()).isEqualTo((byte) 42);
    }

    @Test
    void of_Hex()
    {
        // given
        String h = "2A";

        // when
        var result = Value.ofHex(h);

        // then
        assertThat(result.data()).isEqualTo((byte) 42);
    }

    @Test
    void of_Bits()
    {
        // given
        String b = "00101010";

        // when
        var result = Value.ofBits(b);

        // then
        assertThat(result.data()).isEqualTo((byte) 42);
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00",
                "00, FF, 00",
                "FF, FF, FF",
                "FF, 00, 00",
                "04, 07, 04"})
    void and(String initVal, String input, String expected)
    {
        // given
        var value = Value.ofHex(initVal);

        // when
        Value result = value.and(Value.ofHex(input));

        // then
        assertThat(result).isEqualTo(Value.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00",
                "00, FF, FF",
                "FF, FF, FF",
                "FF, 00, FF",
                "04, 07, 07"})
    void or(String initVal, String input, String expected)
    {
        // given
        var value = Value.ofHex(initVal);

        // when
        Value result = value.or(Value.ofHex(input));

        // then
        assertThat(result).isEqualTo(Value.ofHex(expected));
    }

    @Test
    void toStringTest()
    {
        // given
        var value = Value.of(0x12);

        // when
        String result = value.toString();

        // then
        assertThat(result).isEqualTo(Value.class.getSimpleName() + "[0x12]");
    }

    @ParameterizedTest
    @CsvSource({"00, FF",
                "01, 00"})
    void decrement(String init, String expected)
    {
        // given
        var value = Value.ofHex(init);

        // when
        Value result = value.decrement();

        // then
        assertThat(result).isEqualTo(Value.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"00, 01",
                "FF, 00"})
    void increment(String init, String expected)
    {
        // given
        var value = Value.ofHex(init);

        // when
        Value result = value.increment();

        // then
        assertThat(result).isEqualTo(Value.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"00, false",
                "01, false",
                "FF, true"})
    void isNegative(String init, boolean expected)
    {
        // given
        var value = Value.ofHex(init);

        // when
        boolean result = value.isNegative();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"00, true",
                "01, false",
                "FF, false"})
    void isZero(String init, boolean expected)
    {
        // given
        var value = Value.ofHex(init);

        // when
        boolean result = value.isZero();

        // then
        assertThat(result).isEqualTo(expected);
    }
}
