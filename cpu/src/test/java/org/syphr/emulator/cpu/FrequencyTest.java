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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

class FrequencyTest
{
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "1", "foo", "hz", "2ghz", "-1hz"})
    void of_InvalidFrequency_ThrowsException(String frequency)
    {
        // when
        Exception result = catchException(() -> ClockSignal.Frequency.of(frequency));

        // then
        assertThat(result).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {" 1hz", "1hz", "1hz ", "1 hz", "2hz", "1khz", "1mhz"})
    void of_ValidFrequency_ReturnsClockSpeed(String frequency)
    {
        // when
        var result = ClockSignal.Frequency.of(frequency);

        // then
        assertThat(result).isNotNull();
    }
}
