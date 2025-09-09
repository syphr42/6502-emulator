/*
 * Copyright © 2025 Gregory P. Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.syphr.emulator.cpu;

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

    @ParameterizedTest
    @CsvSource({"00, 00, 00",
                "00, FF, FF",
                "FF, FF, 00",
                "FF, 00, FF",
                "04, 07, 03"})
    void xor(String initVal, String input, String expected)
    {
        // given
        var value = Value.ofHex(initVal);

        // when
        Value result = value.xor(Value.ofHex(input));

        // then
        assertThat(result).isEqualTo(Value.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"00, FF",
                "FF, 00",
                "12, ED"})
    void not(String initVal, String expected)
    {
        // given
        var value = Value.ofHex(initVal);

        // when
        Value result = value.not();

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
    @CsvSource({"00000000, 0, 00000001",
                "00000001, 0, 00000001",
                "11111110, 0, 11111111",
                "11111111, 0, 11111111",
                "00000000, 1, 00000010",
                "00000010, 1, 00000010",
                "11111101, 1, 11111111",
                "11111111, 1, 11111111",
                "00000000, 2, 00000100",
                "00000100, 2, 00000100",
                "11111011, 2, 11111111",
                "11111111, 2, 11111111",
                "00000000, 3, 00001000",
                "00001000, 3, 00001000",
                "11110111, 3, 11111111",
                "11111111, 3, 11111111",
                "00000000, 4, 00010000",
                "00010000, 4, 00010000",
                "11101111, 4, 11111111",
                "11111111, 4, 11111111",
                "00000000, 5, 00100000",
                "00100000, 5, 00100000",
                "11011111, 5, 11111111",
                "11111111, 5, 11111111",
                "00000000, 6, 01000000",
                "01000000, 6, 01000000",
                "10111111, 6, 11111111",
                "11111111, 6, 11111111",
                "00000000, 7, 10000000",
                "10000000, 7, 10000000",
                "01111111, 7, 11111111",
                "11111111, 7, 11111111"})
    void set(String init, int position, String expected)
    {
        // given
        var value = Value.ofBits(init);

        // when
        Value result = value.set(position);

        // then
        assertThat(result).isEqualTo(Value.ofBits(expected));
    }

    @ParameterizedTest
    @CsvSource({"00000000, 0, 00000000",
                "00000001, 0, 00000000",
                "11111110, 0, 11111110",
                "11111111, 0, 11111110",
                "00000000, 1, 00000000",
                "00000010, 1, 00000000",
                "11111101, 1, 11111101",
                "11111111, 1, 11111101",
                "00000000, 2, 00000000",
                "00000100, 2, 00000000",
                "11111011, 2, 11111011",
                "11111111, 2, 11111011",
                "00000000, 3, 00000000",
                "00001000, 3, 00000000",
                "11110111, 3, 11110111",
                "11111111, 3, 11110111",
                "00000000, 4, 00000000",
                "00010000, 4, 00000000",
                "11101111, 4, 11101111",
                "11111111, 4, 11101111",
                "00000000, 5, 00000000",
                "00100000, 5, 00000000",
                "11011111, 5, 11011111",
                "11111111, 5, 11011111",
                "00000000, 6, 00000000",
                "01000000, 6, 00000000",
                "10111111, 6, 10111111",
                "11111111, 6, 10111111",
                "00000000, 7, 00000000",
                "10000000, 7, 00000000",
                "01111111, 7, 01111111",
                "11111111, 7, 01111111"})
    void clear(String init, int position, String expected)
    {
        // given
        var value = Value.ofBits(init);

        // when
        Value result = value.clear(position);

        // then
        assertThat(result).isEqualTo(Value.ofBits(expected));
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

    @ParameterizedTest
    @CsvSource({"00000000, 0, false",
                "00000001, 0, true",
                "00000010, 0, false",
                "00000100, 0, false",
                "00001000, 0, false",
                "00010000, 0, false",
                "00100000, 0, false",
                "01000000, 0, false",
                "10000000, 0, false",
                "00000000, 1, false",
                "00000001, 1, false",
                "00000010, 1, true",
                "00000100, 1, false",
                "00001000, 1, false",
                "00010000, 1, false",
                "00100000, 1, false",
                "01000000, 1, false",
                "10000000, 1, false",
                "00000000, 2, false",
                "00000001, 2, false",
                "00000010, 2, false",
                "00000100, 2, true",
                "00001000, 2, false",
                "00010000, 2, false",
                "00100000, 2, false",
                "01000000, 2, false",
                "10000000, 2, false",
                "00000000, 3, false",
                "00000001, 3, false",
                "00000010, 3, false",
                "00000100, 3, false",
                "00001000, 3, true",
                "00010000, 3, false",
                "00100000, 3, false",
                "01000000, 3, false",
                "10000000, 3, false",
                "00000000, 4, false",
                "00000001, 4, false",
                "00000010, 4, false",
                "00000100, 4, false",
                "00001000, 4, false",
                "00010000, 4, true",
                "00100000, 4, false",
                "01000000, 4, false",
                "10000000, 4, false",
                "00000000, 5, false",
                "00000001, 5, false",
                "00000010, 5, false",
                "00000100, 5, false",
                "00001000, 5, false",
                "00010000, 5, false",
                "00100000, 5, true",
                "01000000, 5, false",
                "10000000, 5, false",
                "00000000, 6, false",
                "00000001, 6, false",
                "00000010, 6, false",
                "00000100, 6, false",
                "00001000, 6, false",
                "00010000, 6, false",
                "00100000, 6, false",
                "01000000, 6, true",
                "10000000, 6, false",
                "00000000, 7, false",
                "00000001, 7, false",
                "00000010, 7, false",
                "00000100, 7, false",
                "00001000, 7, false",
                "00010000, 7, false",
                "00100000, 7, false",
                "01000000, 7, false",
                "10000000, 7, true"})
    void isSet(String init, int position, boolean expected)
    {
        // given
        var value = Value.ofBits(init);

        // when
        boolean result = value.isSet(position);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"00, 01, 01",
                "00, FF, FF",
                "F0, 0F, FF",
                "FF, 01, 00",
                "F0, 10, 00"})
    void plus(String input, String displacement, String expected)
    {
        // given
        var start = Value.ofHex(input);

        // when
        Value result = start.plus(Value.ofHex(displacement));

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
        assertThat(result).isEqualTo("0x12");
    }
}
