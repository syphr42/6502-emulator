package org.syphr.cpu6502.emulator.machine;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ClockSpeed(Duration cycle)
{
    public static final ClockSpeed ONE_HZ = new ClockSpeed(Duration.ofSeconds(1));
    public static final ClockSpeed ONE_KHZ = new ClockSpeed(Duration.ofMillis(1));
    public static final ClockSpeed ONE_MHZ = new ClockSpeed(Duration.ofNanos(1000));

    private static final Pattern FREQUENCY_PATTERN = Pattern.compile("^\\s*(\\d{1,2})\\s*([mk]?hz)\\s*$");

    public static ClockSpeed of(String frequency)
    {
        Matcher m = FREQUENCY_PATTERN.matcher(frequency.toLowerCase());
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid clock frequency: " + frequency);
        }

        var baseClock = switch (m.group(2)) {
            case "hz" -> ONE_HZ;
            case "khz" -> ONE_KHZ;
            case "mhz" -> ONE_MHZ;
            default -> throw new IllegalArgumentException("Invalid clock frequency: " + frequency);
        };

        try {
            int multiplier = Integer.parseInt(m.group(1));
            return baseClock.times(multiplier);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid clock frequency: " + frequency);
        }
    }

    public ClockSpeed times(int multiplier)
    {
        return new ClockSpeed(this.cycle.dividedBy(multiplier));
    }
}
