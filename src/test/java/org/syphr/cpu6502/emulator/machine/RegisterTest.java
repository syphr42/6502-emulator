package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterTest
{
    Register register;

    @BeforeEach
    void beforeEach()
    {
        register = new Register();
    }

    @ParameterizedTest
    @CsvSource({"00, FF",
                "01, 00"})
    void decrement(String initReg, String expected)
    {
        // given
        register.store(Value.ofHex(initReg));

        // when
        register.decrement();

        // then
        assertThat(register.value()).isEqualTo(Value.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"00, 01",
                "FF, 00"})
    void increment(String initReg, String expected)
    {
        // given
        register.store(Value.ofHex(initReg));

        // when
        register.increment();

        // then
        assertThat(register.value()).isEqualTo(Value.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"00, false",
                "01, false",
                "FF, true"})
    void isNegative(String initReg, boolean expected)
    {
        // given
        register.store(Value.ofHex(initReg));

        // when
        boolean result = register.isNegative();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"00, true",
                "01, false",
                "FF, false"})
    void isZero(String initReg, boolean expected)
    {
        // given
        register.store(Value.ofHex(initReg));

        // when
        boolean result = register.isZero();

        // then
        assertThat(result).isEqualTo(expected);
    }
}
