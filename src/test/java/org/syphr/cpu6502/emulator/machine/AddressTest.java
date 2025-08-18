package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.Test;

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

    @Test
    void of_LowHigh()
    {
        // given
        var high = Value.ofHex("12");
        var low = Value.ofHex("34");

        // when
        var result = Address.of(low, high);

        // then
        assertThat(result).isEqualTo(Address.ofHex("1234"));
    }

    @Test
    void values()
    {
        // given
        var address = Address.ofHex("1234");

        // when
        List<Value> result = address.values();

        // then
        assertThat(result).containsExactly(Value.ofHex("34"), Value.ofHex("12"));
    }
}
