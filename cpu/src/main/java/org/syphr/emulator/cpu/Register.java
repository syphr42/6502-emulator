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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.BitSet;

@Slf4j
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Register
{
    @ToString.Include
    private Value value = Value.ZERO;

    public void decrement()
    {
        value = value.decrement();
    }

    public void increment()
    {
        value = value.increment();
    }

    public boolean isNegative()
    {
        return value.isNegative();
    }

    public boolean isZero()
    {
        return value.isZero();
    }

    public void store(Value value)
    {
        this.value = value;
    }

    public Value value()
    {
        return value;
    }

    public BitSet toBits()
    {
        return BitSet.valueOf(new byte[] {value.data()});
    }
}
