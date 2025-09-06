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

import java.util.List;

public record Address(short data) implements Comparable<Address>
{
    public static final Address NMI = Address.of(0xFFFA);
    public static final Address RESET = Address.of(0xFFFC);
    public static final Address IRQ = Address.of(0xFFFE);

    public static Address of(short s)
    {
        return new Address(s);
    }

    public static Address of(int i)
    {
        return new Address((short) i);
    }

    public static Address ofHex(String hex)
    {
        return new Address((short) Integer.parseInt(hex, 16));
    }

    public static Address ofBits(String bits)
    {
        return new Address((short) Integer.parseInt(bits, 2));
    }

    public static Address of(Value low, Value high)
    {
        return Address.of((short) ((high.data() << 8) | (low.data() & 0xFF)));
    }

    public static Address zeroPage(Value offset)
    {
        return Address.of(offset, Value.ZERO);
    }

    public Address increment()
    {
        return Address.of(data + 1);
    }

    public Address decrement()
    {
        return Address.of(data - 1);
    }

    public Address plus(Value v)
    {
        return Address.of(data + v.data());
    }

    public Address plusUnsigned(Value v)
    {
        return Address.of(data + Byte.toUnsignedInt(v.data()));
    }

    public Value low()
    {
        return Value.of(data);
    }

    public Value high()
    {
        return Value.of(data >> 8);
    }

    public List<Value> bytes()
    {
        return List.of(low(), high());
    }

    @Override
    public int compareTo(Address o)
    {
        return Short.compareUnsigned(data, o.data);
    }

    @Override
    public String toString()
    {
        return Address.class.getSimpleName() + "[0x%04X]".formatted(data);
    }
}
