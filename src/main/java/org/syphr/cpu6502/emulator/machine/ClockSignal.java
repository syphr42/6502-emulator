package org.syphr.cpu6502.emulator.machine;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionalInterface
public interface ClockSignal
{
    void await() throws InterruptedException;

    record Frequency(Duration cycle) implements ClockSignal
    {
        public static final Frequency ONE_HZ = new Frequency(Duration.ofSeconds(1));
        public static final Frequency ONE_KHZ = new Frequency(Duration.ofMillis(1));
        public static final Frequency ONE_MHZ = new Frequency(Duration.ofNanos(1000));

        private static final Pattern FREQUENCY_PATTERN = Pattern.compile("^\\s*(\\d{1,2})\\s*([mk]?hz)\\s*$");

        public static Frequency of(String frequency)
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

        public Frequency times(int multiplier)
        {
            return new Frequency(this.cycle.dividedBy(multiplier));
        }

        @Override
        public void await() throws InterruptedException
        {
            Thread.sleep(cycle);
        }
    }
}
