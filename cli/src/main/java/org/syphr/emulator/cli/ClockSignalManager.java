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
package org.syphr.emulator.cli;

import lombok.extern.slf4j.Slf4j;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.syphr.emulator.cpu.ClockSignal;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.jline.keymap.KeyMap.key;

@Slf4j
public class ClockSignalManager implements ClockSignal
{
    private final Lock lock = new ReentrantLock();
    private final Condition step = lock.newCondition();

    boolean takeStep = false;

    private final AtomicReference<Frequency> frequency;
    private final AtomicReference<Boolean> stepping;

    private final Terminal terminal;
    private final Attributes saveTermAttributes;

    private final BindingReader bindingReader;
    private final KeyMap<Action> keyMap;

    public ClockSignalManager(Terminal terminal, Frequency frequency, boolean stepping)
    {
        this.frequency = new AtomicReference<>(frequency);
        this.stepping = new AtomicReference<>(stepping);

        this.terminal = terminal;
        saveTermAttributes = terminal.enterRawMode();
        terminal.puts(InfoCmp.Capability.keypad_xmit);
        terminal.flush();

        bindingReader = new BindingReader(terminal.reader());

        keyMap = new KeyMap<>();
        keyMap.bind(Action.DECREASE_FREQUENCY, key(terminal, InfoCmp.Capability.key_left));
        keyMap.bind(Action.INCREASE_FREQUENCY, key(terminal, InfoCmp.Capability.key_right));
        keyMap.bind(Action.STEP, "\r");
        keyMap.bind(Action.TOGGLE_STEPPING, " ");

        var thread = new Thread(this::checkForSignalUpdate, this.getClass().getSimpleName());
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void await() throws InterruptedException
    {
        if (stepping.get()) {
            awaitStep();
        } else {
            frequency.get().await();
        }
    }

    private void awaitStep() throws InterruptedException
    {
        lock.lock();
        try {
            while (!takeStep) {
                step.await();
            }
            takeStep = false;
        } finally {
            lock.unlock();
        }
    }

    private void checkForSignalUpdate()
    {
        while (!Thread.interrupted()) {
            Action action = bindingReader.readBinding(keyMap);

            lock.lock();
            try {
                switch (action) {
                    case STEP -> {
                        takeStep = true;
                        step.signal();
                    }
                    case TOGGLE_STEPPING -> {
                        stepping.getAndUpdate(b -> !b);
                        takeStep = true;
                        step.signal();
                    }
                    case INCREASE_FREQUENCY -> frequency.getAndUpdate(f -> f.times(2));
                    case DECREASE_FREQUENCY -> frequency.getAndUpdate(f -> f.dividedBy(2));
                    case null -> {}
                    default -> {}
                }
            } finally {
                lock.unlock();
            }
        }

        terminal.setAttributes(saveTermAttributes);
        terminal.puts(InfoCmp.Capability.keypad_local);
        terminal.flush();
    }

    private enum Action
    {
        TOGGLE_STEPPING, STEP, INCREASE_FREQUENCY, DECREASE_FREQUENCY
    }
}
