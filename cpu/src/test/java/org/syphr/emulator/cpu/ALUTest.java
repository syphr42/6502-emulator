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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.syphr.emulator.common.Register;
import org.syphr.emulator.common.Value;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ALUTest
{
    @Tag("slow")
    @ParameterizedTest
    @CsvFileSource(resources = "/org/syphr/emulator/cpu/alu_adc_binary.csv", numLinesToSkip = 1)
    void addWithCarryBinaryMode(int givenAccumulator,
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
        status.setDecimal(false).setCarry(givenCarry != 0);
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

    @Tag("slow")
    @ParameterizedTest
    @CsvFileSource(resources = "/org/syphr/emulator/cpu/alu_adc_decimal.csv", numLinesToSkip = 1)
    void addWithCarryDecimalMode(int givenAccumulator,
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
        status.setDecimal(true).setCarry(givenCarry != 0);
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

    @Tag("slow")
    @ParameterizedTest
    @CsvFileSource(resources = "/org/syphr/emulator/cpu/alu_sbc_binary.csv", numLinesToSkip = 1)
    void subtractWithCarryBinaryMode(int givenAccumulator,
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
        status.setDecimal(false).setCarry(givenCarry != 0);
        var alu = new ALU(status);

        var accumulator = new Register();
        accumulator.load(Value.of(givenAccumulator));

        var value = Value.of(input);

        // when
        alu.subtractWithCarry(accumulator, value);

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

    @Tag("slow")
    @ParameterizedTest
    @CsvFileSource(resources = "/org/syphr/emulator/cpu/alu_sbc_decimal.csv", numLinesToSkip = 1)
    void subtractWithCarryDecimalMode(int givenAccumulator,
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
        status.setDecimal(true).setCarry(givenCarry != 0);
        var alu = new ALU(status);

        var accumulator = new Register();
        accumulator.load(Value.of(givenAccumulator));

        var value = Value.of(input);

        // when
        alu.subtractWithCarry(accumulator, value);

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

    // call if needed to regen test data
    private void generateDate(boolean decimal, boolean add) throws IOException
    {
        var dir = "src/test/resources/org/syphr/emulator/cpu";
        var filename = "alu_" + (add ? "adc" : "sbc") + "_" + (decimal ? "decimal" : "binary") + ".csv";

        try (var out = new BufferedWriter(new FileWriter("%s/%s".formatted(dir, filename)))) {
            out.write(
                    "givenAccumulator,givenCarry,input,expectedAccumulator,expectedNegative,expectedOverflow,expectedZero,expectedCarry");
            out.newLine();

            for (int carry : IntStream.rangeClosed(0, 1).toArray()) {
                for (int n1 : IntStream.rangeClosed(0, 255).toArray()) {
                    for (int n2 : IntStream.rangeClosed(0, 255).toArray()) {
                        Value n1Value = Value.of(n1);
                        Value n2Value = Value.of(n2);

                        var status = new StatusRegister().setDecimal(decimal).setCarry(carry != 0);
                        var alu = new ALU(status);

                        var accumulator = new Register();
                        accumulator.load(n1Value);

                        if (add) {
                            alu.addWithCarry(accumulator, n2Value);
                        } else {
                            alu.subtractWithCarry(accumulator, n2Value);
                        }

                        out.write("%s,%d,%s,%d,%b,%b,%b,%b".formatted(n1Value,
                                                                      carry,
                                                                      n2Value,
                                                                      accumulator.value().data(),
                                                                      status.negative(),
                                                                      status.overflow(),
                                                                      status.zero(),
                                                                      status.carry()));
                        out.newLine();
                    }
                }
            }
        }
    }
}
