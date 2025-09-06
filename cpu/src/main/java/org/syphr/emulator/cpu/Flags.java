package org.syphr.emulator.cpu;

import lombok.Builder;

@Builder(toBuilder = true)
public record Flags(boolean negative,
                    boolean overflow,
                    boolean user,
                    boolean breakCommand,
                    boolean decimal,
                    boolean irqDisable,
                    boolean zero,
                    boolean carry) {}
