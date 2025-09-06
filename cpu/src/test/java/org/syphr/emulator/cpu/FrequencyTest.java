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
