package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.Test;

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
}
