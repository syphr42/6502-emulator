/*
 * Copyright © 2025-2026 Gregory P. Moyer
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

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

@Slf4j
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
            updateLoggingContext();

            fireCycleStarted();
            log.trace("Clock signal {} started", cycleCount);
            long cycleStartTime = System.nanoTime();

            if (cycle == breakAfterCycle) {
                stepping.set(true);
            }

            try {
                if (stepping.get()) {
                    awaitStep();
                } else {
                    Duration duration = period.get();
                    // spinWait typically executes much faster than sleep, but can consume more CPU
                    if (duration.toNanos() < 60_000) {
                        spinWait(duration);
                    } else {
                        sleep(duration);
                    }
                }
            } catch (InterruptedException e) {
                break;
            }

            log.atTrace()
               .setMessage("Clock signal {} completed; runtime: {} ns")
               .addArgument(cycleCount)
               .addArgument(() -> System.nanoTime() - cycleStartTime)
               .log();
            fireCycleEnded();
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

    public void setPeriod(Duration duration)
    {
        period.set(duration);
    }

    private void updateLoggingContext()
    {
        MDC.put("clock", String.valueOf(cycleCount));
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

    private void sleep(Duration duration) throws InterruptedException
    {
        Thread.sleep(duration);
    }

    private void spinWait(Duration duration)
    {
        long start = System.nanoTime();
        while (System.nanoTime() - start < duration.toNanos()) {
            Thread.onSpinWait();
        }
    }

    private void fireCycleStarted()
    {
        ClockEvent event = null;
        for (ClockListener listener : listeners) {
            if (event == null) {
                event = new ClockEvent(cycleCount);
            }
            listener.cycleStarted(event);
        }
    }

    private void fireCycleEnded()
    {
        ClockEvent event = null;
        for (ClockListener listener : listeners) {
            if (event == null) {
                event = new ClockEvent(cycleCount);
            }
            listener.cycleEnded(event);
        }
    }
}
