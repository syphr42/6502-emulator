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
package org.syphr.emulator.via;

import org.syphr.emulator.common.Value;

public class VIA
{
    private final Port a = new Port();
    private final Port b = new Port();

    public Value read(RegisterSelector selector)
    {
        return switch (selector) {
            case PORT_A_DATA -> a.getInput();
            case PORT_A_DIRECTION -> a.getDataDirection();
            case PORT_B_DATA -> b.getInput();
            case PORT_B_DIRECTION -> b.getDataDirection();
            // TODO

            default -> throw new UnsupportedOperationException();
        };
    }

    public void write(RegisterSelector selector, Value value)
    {
        switch (selector) {
            case PORT_A_DATA -> a.setOutput(value);
            case PORT_A_DIRECTION -> a.setDataDirection(value);
            case PORT_B_DATA -> b.setOutput(value);
            case PORT_B_DIRECTION -> b.setDataDirection(value);
            // TODO

            default -> throw new UnsupportedOperationException();
        }
    }
}
