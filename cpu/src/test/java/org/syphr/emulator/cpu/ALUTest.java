/*
 * Copyright © 2025-2026 Gregory P. Moyer
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.syphr.emulator.common.Register;
import org.syphr.emulator.common.Value;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ALUTest
{
    static Stream<Arguments> addWithCarry()
    {
        return Stream.of(Arguments.of(0x58, 1, false, 0x46, 0x9F, true, true, false, false),
                         Arguments.of(0x58, 1, true, 0x46, 0x05, false, false, false, true),
                         Arguments.of(0x12, 0, true, 0x34, 0x46, false, false, false, false),
                         Arguments.of(0x15, 0, true, 0x26, 0x41, false, false, false, false),
                         Arguments.of(0x81, 0, true, 0x92, 0x73, false, false, false, true));
    }

    @ParameterizedTest
    @MethodSource
    void addWithCarry(int givenAccumulator,
                      int givenCarry,
                      boolean givenDecimal,
                      int input,
                      int expectedAccumulator,
                      boolean expectedNegative,
                      boolean expectedOverflow,
                      boolean expectedZero,
                      boolean expectedCarry)
    {
        // given
        var status = new StatusRegister();
        status.setDecimal(givenDecimal).setCarry(givenCarry != 0);
        var alu = new ALU(status);

        var accumulator = new Register();
        accumulator.load(Value.of(givenAccumulator));

        var value = Value.of(input);

        // when
        alu.addWithCarry(accumulator, value);

        // then
        assertAll(
                () -> assertEquals(Value.of(expectedAccumulator), accumulator.value()),
                () -> assertEquals(status.flags().toBuilder().negative(expectedNegative)
                                         .overflow(expectedOverflow)
                                         .zero(expectedZero)
                                         .carry(expectedCarry)
                                         .build(), status.flags())
        );
    }
}
