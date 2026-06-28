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
        return Stream.of(Arguments.of(false, 0x58, 1, 0x46, 0x9F, true, true, false, false),
                         Arguments.of(true, 0x93, 0, 0x82, 0x75, false, true, false, true),
                         Arguments.of(true, 0x58, 1, 0x46, 0x05, false, true, false, true),
                         Arguments.of(true, 0x12, 0, 0x34, 0x46, false, false, false, false),
                         Arguments.of(true, 0x15, 0, 0x26, 0x41, false, false, false, false),
                         Arguments.of(true, 0x81, 0, 0x92, 0x73, false, true, false, true),
                         Arguments.of(true, 0x00, 0, 0x00, 0x00, false, false, true, false),
                         Arguments.of(true, 0x79, 1, 0x00, 0x80, true, true, false, false),
                         Arguments.of(true, 0x24, 0, 0x56, 0x80, true, true, false, false),
                         Arguments.of(true, 0x93, 0, 0x82, 0x75, false, true, false, true),
                         Arguments.of(true, 0x89, 0, 0x76, 0x65, false, false, false, true),
                         Arguments.of(true, 0x89, 1, 0x76, 0x66, false, false, false, true),
                         Arguments.of(true, 0x80, 0, 0xf0, 0xd0, true, true, false, true),
                         Arguments.of(true, 0x80, 0, 0xfa, 0xe0, true, false, false, true),
                         Arguments.of(true, 0x2f, 0, 0x4f, 0x74, false, false, false, false),
                         Arguments.of(true, 0x6f, 1, 0x00, 0x76, false, false, false, false));
    }

    @ParameterizedTest
    @MethodSource
    void addWithCarry(boolean givenDecimal,
                      int givenAccumulator,
                      int givenCarry,
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
