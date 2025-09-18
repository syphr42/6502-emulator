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

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClockPeriod
{
    public static final Duration ONE_HZ = Duration.ofSeconds(1);
    public static final Duration ONE_KHZ = Duration.ofMillis(1);
    public static final Duration ONE_MHZ = Duration.ofNanos(1000);

    private static final Pattern FREQUENCY_PATTERN = Pattern.compile("^\\s*(\\d{1,3})\\s*([mk]?hz)\\s*$");

    public static Duration of(String frequency)
    {
        Matcher m = FREQUENCY_PATTERN.matcher(frequency.toLowerCase());
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid clock frequency: " + frequency);
        }

        Duration basePeriod = switch (m.group(2)) {
            case "hz" -> ONE_HZ;
            case "khz" -> ONE_KHZ;
            case "mhz" -> ONE_MHZ;
            default -> throw new IllegalArgumentException("Invalid clock frequency: " + frequency);
        };

        try {
            int frequencyMultiplier = Integer.parseInt(m.group(1));
            return basePeriod.dividedBy(frequencyMultiplier);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid clock frequency: " + frequency);
        }
    }

    private ClockPeriod()
    {
    }
}
