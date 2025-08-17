package org.syphr.cpu6502.emulator.machine;

import lombok.Builder;

@Builder(toBuilder = true)
public record Flags(boolean negative,
                    boolean overflow,
                    boolean user1,
                    boolean user2,
                    boolean decimal,
                    boolean irqDisable,
                    boolean zero,
                    boolean carry)
{}
