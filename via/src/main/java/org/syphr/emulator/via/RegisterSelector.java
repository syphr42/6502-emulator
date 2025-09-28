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

public enum RegisterSelector
{
    PORT_B_DATA,
    PORT_A_DATA,
    PORT_B_DIRECTION,
    PORT_A_DIRECTION,
    TIMER_1_COUNTER_LOW,
    TIMER_1_COUNTER_HIGH,
    TIMER_1_LATCH_LOW,
    TIMER_1_LATCH_HIGH,
    TIMER_2_COUNTER_LOW,
    TIMER_2_COUNTER_HIGH,
    SHIFT,
    AUXILIARY_CONTROL,
    PERIPHERAL_CONTROL,
    INTERRUPT_FLAG,
    INTERRUPT_ENABLE,
    PORT_B_DATA_NO_HANDSHAKE;

    public static RegisterSelector of(int selector)
    {
        return switch (selector) {
            case 0b0000 -> PORT_B_DATA;
            case 0b0001 -> PORT_A_DATA;
            case 0b0010 -> PORT_B_DIRECTION;
            case 0b0011 -> PORT_A_DIRECTION;
            case 0b0100 -> TIMER_1_COUNTER_LOW;
            case 0b0101 -> TIMER_1_COUNTER_HIGH;
            case 0b0110 -> TIMER_1_LATCH_LOW;
            case 0b0111 -> TIMER_1_LATCH_HIGH;
            case 0b1000 -> TIMER_2_COUNTER_LOW;
            case 0b1001 -> TIMER_2_COUNTER_HIGH;
            case 0b1010 -> SHIFT;
            case 0b1011 -> AUXILIARY_CONTROL;
            case 0b1100 -> PERIPHERAL_CONTROL;
            case 0b1101 -> INTERRUPT_FLAG;
            case 0b1110 -> INTERRUPT_ENABLE;
            case 0b1111 -> PORT_B_DATA_NO_HANDSHAKE;

            default -> throw new IllegalArgumentException("Selector must be 0-15 (4 bits)");
        };
    }
}
