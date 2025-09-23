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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StatusRegisterTest
{
    StatusRegister status;

    @BeforeEach
    void beforeEach()
    {
        status = new StatusRegister();
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
    void of(String bits,
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
        StatusRegister result = StatusRegister.of(flags);

        // then
        status.load(Value.ofBits(bits));
        assertThat(result).isEqualTo(status);
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
    void flags(String bits,
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
        status.load(Value.ofBits(bits));

        // when
        Flags result = status.flags();

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
    @ValueSource(booleans = {false, true})
    void setNegative(boolean input)
    {
        // when
        status.setNegative(input);

        // then
        assertThat(status.negative()).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void setOverflow(boolean input)
    {
        // when
        status.setOverflow(input);

        // then
        assertThat(status.overflow()).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void setUser(boolean input)
    {
        // when
        status.setUser(input);

        // then
        assertThat(status.user()).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void setBreakCommand(boolean input)
    {
        // when
        status.setBreakCommand(input);

        // then
        assertThat(status.breakCommand()).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void setDecimal(boolean input)
    {
        // when
        status.setDecimal(input);

        // then
        assertThat(status.decimal()).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void setIrqDisable(boolean input)
    {
        // when
        status.setIrqDisable(input);

        // then
        assertThat(status.irqDisable()).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void setZero(boolean input)
    {
        // when
        status.setZero(input);

        // then
        assertThat(status.zero()).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void setCarry(boolean input)
    {
        // when
        status.setCarry(input);

        // then
        assertThat(status.carry()).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(ints = {0b11111111,
                         0b01111111,
                         0b00111111,
                         0b00011111,
                         0b00001111,
                         0b00000111,
                         0b00000011,
                         0b00000001,
                         0b00000000})
    void copy(int value)
    {
        // given
        status.load(Value.of(value));

        // when
        StatusRegister result = status.copy();

        // then
        assertThat(result).isEqualTo(status);
    }
}
