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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe representation of the state of the interrupt inputs.
 */
class HardwareInterruptState
{
    private final AtomicBoolean nmi = new AtomicBoolean();
    private final AtomicBoolean reset = new AtomicBoolean();
    private final AtomicBoolean irq = new AtomicBoolean();

    public void nmi()
    {
        nmi.set(true);
    }

    public void reset()
    {
        reset.set(true);
    }

    public void irq(boolean state)
    {
        irq.set(state);
    }

    public Optional<Interrupt.HarwareInterrupt> poll()
    {
        if (reset.compareAndExchange(true, false)) {
            return Optional.of(Interrupt.HarwareInterrupt.RESET);
        }

        if (nmi.compareAndExchange(true, false)) {
            return Optional.of(Interrupt.HarwareInterrupt.NMI);
        }

        if (irq.get()) {
            return Optional.of(Interrupt.HarwareInterrupt.IRQ);
        }

        return Optional.empty();
    }
}
