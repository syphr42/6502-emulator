package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest
{
    @Test
    void of_Short()
    {
        // given
        short s = 542;

        // when
        var result = Address.of(s);

        // then
        assertThat(result.data()).isEqualTo((short) 542);
    }

    @Test
    void of_Int()
    {
        // given
        int i = 542;

        // when
        var result = Address.of(i);

        // then
        assertThat(result.data()).isEqualTo((short) 542);
    }

    @Test
    void of_Hex()
    {
        // given
        String h = "21E";

        // when
        var result = Address.ofHex(h);

        // then
        assertThat(result.data()).isEqualTo((short) 542);
    }

    @Test
    void of_Bits()
    {
        // given
        String b = "1000011110";

        // when
        var result = Address.ofBits(b);

        // then
        assertThat(result.data()).isEqualTo((short) 542);
    }

    @ParameterizedTest
    @CsvSource({"12, 34, 1234",
                "00, FD, 00FD",
                "FF, 00, FF00"})
    void of_LowHigh(String highByte, String lowByte, String expected)
    {
        // given
        var high = Value.ofHex(highByte);
        var low = Value.ofHex(lowByte);

        // when
        var result = Address.of(low, high);

        // then
        assertThat(result).isEqualTo(Address.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"0000, 0001",
                "FFFF, 0000"})
    void increment(String input, String expected)
    {
        // given
        var start = Address.ofHex(input);

        // when
        Address result = start.increment();

        // then
        assertThat(result).isEqualTo(Address.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"0001, 0000",
                "0000, FFFF"})
    void decrement(String input, String expected)
    {
        // given
        var start = Address.ofHex(input);

        // when
        Address result = start.decrement();

        // then
        assertThat(result).isEqualTo(Address.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"0000, 01, 0001",
                "0000, FF, 00FF",
                "00F0, 0F, 00FF",
                "FFFF, 01, 0000",
                "FFF0, 10, 0000"})
    void plus(String input, String displacement, String expected)
    {
        // given
        var start = Address.ofHex(input);

        // when
        Address result = start.plus(Value.ofHex(displacement));

        // then
        assertThat(result).isEqualTo(Address.ofHex(expected));
    }

    @Test
    void low()
    {
        // given
        var address = Address.of(0x1234);

        // when
        Value result = address.low();

        // then
        assertThat(result).isEqualTo(Value.of(0x34));
    }

    @Test
    void high()
    {
        // given
        var address = Address.of(0x1234);

        // when
        Value result = address.high();

        // then
        assertThat(result).isEqualTo(Value.of(0x12));
    }

    @Test
    void bytes()
    {
        // given
        var address = Address.of(0x1234);

        // when
        List<Value> result = address.bytes();

        // then
        assertThat(result).containsExactly(Value.of(0x34), Value.of(0x12));
    }

    @Test
    void toStringTest()
    {
        // given
        var address = Address.of(0x1234);

        // when
        String result = address.toString();

        // then
        assertThat(result).isEqualTo(Address.class.getSimpleName() + "[0x1234]");
    }
}
