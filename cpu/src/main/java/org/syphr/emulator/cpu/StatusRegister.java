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

import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
class StatusRegister extends Register
{
    private static final int NEGATIVE_BIT_POSITION = 7;
    private static final int OVERFLOW_BIT_POSITION = 6;
    private static final int USER_BIT_POSITION = 5;
    private static final int BREAK_COMMAND_BIT_POSITION = 4;
    private static final int DECIMAL_BIT_POSITION = 3;
    private static final int IRQ_DISABLE_BIT_POSITION = 2;
    private static final int ZERO_BIT_POSITION = 1;
    private static final int CARRY_BIT_POSITION = 0;

    public Flags flags()
    {
        return Flags.builder()
                    .negative(negative())
                    .overflow(overflow())
                    .user(user())
                    .breakCommand(breakCommand())
                    .decimal(decimal())
                    .irqDisable(irqDisable())
                    .zero(zero())
                    .carry(carry())
                    .build();
    }

    @ToString.Include
    public boolean negative()
    {
        return state(NEGATIVE_BIT_POSITION);
    }

    @ToString.Include
    public boolean overflow()
    {
        return state(OVERFLOW_BIT_POSITION);
    }

    @ToString.Include
    public boolean user()
    {
        return state(USER_BIT_POSITION);
    }

    @ToString.Include
    public boolean breakCommand()
    {
        return state(BREAK_COMMAND_BIT_POSITION);
    }

    @ToString.Include
    public boolean decimal()
    {
        return state(DECIMAL_BIT_POSITION);
    }

    @ToString.Include
    public boolean irqDisable()
    {
        return state(IRQ_DISABLE_BIT_POSITION);
    }

    @ToString.Include
    public boolean zero()
    {
        return state(ZERO_BIT_POSITION);
    }

    @ToString.Include
    public boolean carry()
    {
        return state(CARRY_BIT_POSITION);
    }

    public StatusRegister setNegative(boolean b)
    {
        update(b, NEGATIVE_BIT_POSITION);
        return this;
    }

    public StatusRegister setOverflow(boolean b)
    {
        update(b, OVERFLOW_BIT_POSITION);
        return this;
    }

    public StatusRegister setUser(boolean b)
    {
        update(b, USER_BIT_POSITION);
        return this;
    }

    public StatusRegister setBreakCommand(boolean b)
    {
        update(b, BREAK_COMMAND_BIT_POSITION);
        return this;
    }

    public StatusRegister setDecimal(boolean b)
    {
        update(b, DECIMAL_BIT_POSITION);
        return this;
    }

    public StatusRegister setIrqDisable(boolean b)
    {
        update(b, IRQ_DISABLE_BIT_POSITION);
        return this;
    }

    public StatusRegister setZero(boolean b)
    {
        update(b, ZERO_BIT_POSITION);
        return this;
    }

    public StatusRegister setCarry(boolean b)
    {
        update(b, CARRY_BIT_POSITION);
        return this;
    }

    private void update(boolean b, int position)
    {
        if (b) {
            set(position);
        } else {
            clear(position);
        }
    }

    private void set(int position)
    {
        store(Value.of((byte) (value().data() | (0x01 << position))));
    }

    private void clear(int position)
    {
        store(Value.of((byte) (value().data() & ~(0x01 << position))));
    }

    private boolean state(int position)
    {
        return (value().data() & (0x01 << position)) != 0;
    }
}
