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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
class Register
{
    @ToString.Include
    private final AtomicReference<Value> value = new AtomicReference<>(Value.ZERO);

    public void decrement()
    {
        value.getAndUpdate(Value::decrement);
    }

    public void increment()
    {
        value.getAndUpdate(Value::increment);
    }

    public boolean isNegative()
    {
        return value.get().isNegative();
    }

    public boolean isZero()
    {
        return value.get().isZero();
    }

    public void store(Value value)
    {
        this.value.set(value);
    }

    public Value value()
    {
        return value.get();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) { return false; }
        Register register = (Register) o;
        return Objects.equals(value.get(), register.value.get());
    }

    @Override
    public int hashCode()
    {
        return value.get().hashCode();
    }
}
