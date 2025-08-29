package org.syphr.cpu6502.emulator.machine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RequiredArgsConstructor
class Clock implements Runnable
{
    private final Lock lock = new ReentrantLock();
    private final Condition cycle = lock.newCondition();

    private final ClockSignal signal;

    // shared between threads while locked
    private boolean newCycle;

    // not shared
    private long tick;

    public void run()
    {
        while (!Thread.interrupted()) {
            lock.lock();
            try {
                newCycle = true;
                log.info("Clock tick {}", ++tick);
                cycle.signal();
            } finally {
                lock.unlock();
            }

            try {
                signal.await();
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void nextCycle()
    {
        lock.lock();
        try {
            while (!newCycle) {
                cycle.await();
            }
            newCycle = false;
        } catch (InterruptedException e) {
            throw new HaltException("Program interrupted", e);
        } finally {
            lock.unlock();
        }
    }
}
