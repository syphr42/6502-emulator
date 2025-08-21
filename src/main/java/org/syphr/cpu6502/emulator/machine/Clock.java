package org.syphr.cpu6502.emulator.machine;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class Clock
{
    private final ClockSpeed speed;
    private final Runnable action;

    public void start()
    {
        while (!Thread.interrupted()) {
            action.run();

            try {
                Thread.sleep(speed.cycle());
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
