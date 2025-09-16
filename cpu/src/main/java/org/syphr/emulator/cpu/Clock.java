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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RequiredArgsConstructor
class Clock
{
    private final Lock lock = new ReentrantLock();
    private final Condition cycle = lock.newCondition();

    // used only while locked
    private boolean newCycle;

    // mutated while locked
    private long cycleCount;

    public long startNextCycle()
    {
        lock.lock();
        try {
            cycleCount++;
            updateLoggingContext();
            log.info("Clock cycle {}", cycleCount);
            newCycle = true;
            cycle.signal();
        } finally {
            lock.unlock();
        }

        return cycleCount;
    }

    public void awaitNextCycle()
    {
        lock.lock();
        try {
            while (!newCycle) {
                cycle.await();
            }
            updateLoggingContext();
            newCycle = false;
        } catch (InterruptedException e) {
            throw new HaltException("Program interrupted", e);
        } finally {
            lock.unlock();
        }
    }

    private void updateLoggingContext()
    {
        MDC.put("clock", String.valueOf(cycleCount));
    }
}
