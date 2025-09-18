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
package org.syphr.emulator.cli.clock;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

class ClockPeriodTest
{
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "1", "foo", "hz", "2ghz", "-1hz"})
    void of_InvalidFrequency_ThrowsException(String frequency)
    {
        // when
        Exception result = catchException(() -> ClockPeriod.of(frequency));

        // then
        assertThat(result).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"' 1hz',1000000000",
                "1hz,1000000000",
                "'1hz ',1000000000",
                "1 hz,1000000000",
                "2hz,500000000",
                "1khz,1000000",
                "1mhz,1000"})
    void of_ValidFrequency_ReturnsClockSpeed(String frequency, long periodNanos)
    {
        // when
        Duration result = ClockPeriod.of(frequency);

        // then
        assertThat(result).isEqualTo(Duration.ofNanos(periodNanos));
    }
}
