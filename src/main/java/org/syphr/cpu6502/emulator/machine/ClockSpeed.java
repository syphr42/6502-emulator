package org.syphr.cpu6502.emulator.machine;

import java.time.Duration;

public record ClockSpeed(Duration cycle)
{
    public static ClockSpeed ONE_HZ = new ClockSpeed(Duration.ofSeconds(1));
    public static ClockSpeed ONE_KHZ = new ClockSpeed(Duration.ofMillis(1));
    public static ClockSpeed ONE_MHZ = new ClockSpeed(Duration.ofNanos(1000));
    public static ClockSpeed ONE_GHZ = new ClockSpeed(Duration.ofNanos(1));

    public ClockSpeed times(int multiplier)
    {
        return new ClockSpeed(this.cycle.dividedBy(multiplier));
    }
}
