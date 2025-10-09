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

import org.syphr.emulator.common.clock.ClockEvent;
import org.syphr.emulator.common.clock.ClockGenerator;
import org.syphr.emulator.common.clock.ClockListener;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClockSignal implements Runnable, ClockGenerator
{
    private final Lock stepper = new ReentrantLock();
    private final Condition step = stepper.newCondition();
    private boolean takeStep = false;

    private final AtomicReference<Duration> period;
    private final AtomicReference<Boolean> stepping;
    private final long breakAfterCycle;

    private final List<ClockListener> listeners = new CopyOnWriteArrayList<>();

    private long cycleCount = 0;

    public ClockSignal(Duration period, boolean stepping, long breakAfterCycle)
    {
        this.period = new AtomicReference<>(period);
        this.stepping = new AtomicReference<>(stepping);
        this.breakAfterCycle = breakAfterCycle;
    }

    public void addListener(ClockListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(ClockListener listener)
    {
        listeners.remove(listener);
    }

    @Override
    public void run()
    {
        while (!Thread.interrupted()) {
            long cycle = ++cycleCount;
            fireClockTick(new ClockEvent(cycle));

            if (cycle == breakAfterCycle) {
                stepping.set(true);
            }

            try {
                if (stepping.get()) {
                    awaitStep();
                } else {
                    Thread.sleep(period.get());
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void toggleStepping()
    {
        if (stepping.getAndUpdate(b -> !b)) {
            step();
        }
    }

    public void step()
    {
        stepper.lock();
        try {
            takeStep = true;
            step.signal();
        } finally {
            stepper.unlock();
        }
    }

    public void increaseFrequency()
    {
        period.getAndUpdate(p -> p.dividedBy(2));
    }

    public void decreaseFrequency()
    {
        period.getAndUpdate(p -> p.multipliedBy(2));
    }

    private void awaitStep() throws InterruptedException
    {
        stepper.lock();
        try {
            while (!takeStep) {
                step.await();
            }
            takeStep = false;
        } finally {
            stepper.unlock();
        }
    }

    private void fireClockTick(ClockEvent event)
    {
        for (ClockListener listener : listeners) {
            listener.tick(event);
        }
    }
}
