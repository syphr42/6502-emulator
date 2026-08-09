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
package org.syphr.emulator.cpu;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.syphr.emulator.common.clock.ClockEvent;
import org.syphr.emulator.common.clock.ClockListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Slf4j
class Clock
{
    private final List<ClockListener> listeners = new CopyOnWriteArrayList<>();

    private final Lock lock = new ReentrantLock();
    private final Condition cycle = lock.newCondition();
    private final AtomicLong cycleCount = new AtomicLong(0L);
    private final AtomicBoolean ignorePending = new AtomicBoolean(false);

    private volatile long cycleStartTime;

    // used only while locked
    private boolean newCycle;

    /**
     * Signal the clock that it is allowed to start the next cycle as soon as possible.
     */
    public void allowNextCycle()
    {
        lock.lock();
        try {
            if (ignorePending.compareAndExchange(true, false)) {
                return;
            }

            newCycle = true;
            cycle.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Signal the clock to ignore any pending cycle wake-ups. This is useful just after signaling a clock generator to
     * pause in case it has already sent another clock pulse to make the pause more reliable.
     */
    public void ignorePending()
    {
        ignorePending.set(true);
    }

    /**
     * Wait for the specified number of cycles to complete.
     *
     * @param cycleCount the number of cycles to wait for
     */
    public void waitCycles(int cycleCount)
    {
        IntStream.range(0, cycleCount).forEach(this::waitCycle);
    }

    /**
     * Execute the given function in the next cycle. This method will block until the cycle completes.
     * <p>
     * Note that passing in a function which itself requires a cycle to complete (nested cycles) will result in strange
     * logging behavior where a cycle is reportedly started but never completed and a cycle is reportedly completed more
     * than once. A deadlock will not occur as long as all the cycle requests are on the same thread, but this pattern
     * is discouraged because it creates confusing output and the hardware does not work this way.
     *
     * @param fn the function to execute
     */
    public void runCycle(Runnable fn)
    {
        runCycle((Supplier<Void>) () -> {
            fn.run();
            return null;
        });
    }

    /**
     * Execute the given function in the next cycle. This method will block until the cycle completes.
     * <p>
     * Note that passing in a function which itself requires a cycle to complete (nested cycles) will result in strange
     * logging behavior where a cycle is reportedly started but never completed and a cycle is reportedly completed more
     * than once. A deadlock will not occur as long as all the cycle requests are on the same thread, but this pattern
     * is discouraged because it creates confusing output and the hardware does not work this way.
     *
     * @param fn the function to execute
     * @return the result of the function
     */
    public <T> T runCycle(Supplier<T> fn)
    {
        lock.lock();
        try {
            while (!newCycle) {
                cycle.await();
            }

            newCycle = false;
            incrementCycleCount();
            updateLoggingContext();

            fireCycleStarted();
            log.trace("CPU clock cycle {} started", getCycleCount());
            cycleStartTime = System.nanoTime();

            T result = fn.get();

            log.atTrace()
               .setMessage("CPU clock cycle {} completed; runtime: {} ns")
               .addArgument(getCycleCount())
               .addArgument(() -> System.nanoTime() - cycleStartTime)
               .log();
            fireCycleEnded();

            return result;
        } catch (InterruptedException e) {
            throw new HaltException("Program interrupted", e);
        } finally {
            lock.unlock();
        }
    }

    public long getCycleCount()
    {
        return cycleCount.get();
    }

    public void addListener(ClockListener listener)
    {
        listeners.add(listener);
    }

    protected void incrementCycleCount()
    {
        cycleCount.incrementAndGet();
    }

    protected void fireCycleStarted()
    {
        ClockEvent event = null;
        for (ClockListener listener : listeners) {
            if (event == null) {
                event = new ClockEvent(getCycleCount());
            }
            listener.cycleStarted(event);
        }
    }

    protected void fireCycleEnded()
    {
        ClockEvent event = null;
        for (ClockListener listener : listeners) {
            if (event == null) {
                event = new ClockEvent(getCycleCount());
            }
            listener.cycleEnded(event);
        }
    }

    private void updateLoggingContext()
    {
        MDC.put("clock", String.valueOf(getCycleCount()));
    }

    private void waitCycle(int cycle)
    {
        runCycle(EmptySupplier.NOP);
    }

    private static class EmptySupplier implements Supplier<Void>
    {
        public static final EmptySupplier NOP = new EmptySupplier();

        @Override
        public Void get()
        {
            log.info("Waiting one clock cycle for internal CPU operations");
            return null;
        }
    }
}
