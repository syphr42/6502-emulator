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
package org.syphr.emulator.cli.simple;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.syphr.emulator.cli.clock.ClockSignal;

import java.io.IOError;
import java.time.Duration;

import static org.jline.keymap.KeyMap.key;

public class InputManager implements Runnable
{
    private final ClockSignal clockSignal;
    private final Interrupter interrupter;

    private final Terminal terminal;
    private final Attributes saveTermAttributes;

    private final BindingReader bindingReader;
    private final KeyMap<Action> keyMap;

    public InputManager(Terminal terminal, ClockSignal clockSignal, Interrupter interrupter)
    {
        this.clockSignal = clockSignal;
        this.interrupter = interrupter;

        this.terminal = terminal;
        saveTermAttributes = terminal.enterRawMode();
        terminal.puts(InfoCmp.Capability.keypad_xmit);
        terminal.flush();

        bindingReader = new BindingReader(terminal.reader());
        keyMap = new KeyMap<>();

        // Clock
        keyMap.bind(Action.DECREASE_FREQUENCY, key(terminal, InfoCmp.Capability.key_left));
        keyMap.bind(Action.INCREASE_FREQUENCY, key(terminal, InfoCmp.Capability.key_right));
        keyMap.bind(Action.STEP, "\r");
        keyMap.bind(Action.TOGGLE_STEPPING, " ");

        // Interrupts
        keyMap.bind(Action.RESET, "r");
        keyMap.bind(Action.IRQ, "i");
        keyMap.bind(Action.NMI, "n");
    }

    @Override
    public void run()
    {
        while (!Thread.interrupted()) {
            try {
                Action action = bindingReader.readBinding(keyMap);

                switch (action) {
                    // Clock
                    case TOGGLE_STEPPING -> clockSignal.toggleStepping();
                    case STEP -> clockSignal.step();
                    case INCREASE_FREQUENCY -> clockSignal.increaseFrequency();
                    case DECREASE_FREQUENCY -> clockSignal.decreaseFrequency();

                    // Interrupts
                    case RESET -> interrupter.reset();
                    case IRQ -> interrupter.irq();
                    case NMI -> interrupter.nmi();

                    // No event
                    case null -> {}
                }

                Thread.sleep(Duration.ofMillis(500));
            } catch (InterruptedException | IOError e) {
                // IOError thrown when readBinding is interrupted
                break;
            }
        }

        terminal.setAttributes(saveTermAttributes);
        terminal.puts(InfoCmp.Capability.keypad_local);
        terminal.flush();
    }

    private enum Action
    {
        // Clock
        TOGGLE_STEPPING, STEP, INCREASE_FREQUENCY, DECREASE_FREQUENCY,

        // Interrupts
        RESET, IRQ, NMI
    }
}
