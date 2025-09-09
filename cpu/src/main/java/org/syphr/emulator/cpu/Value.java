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

public record Value(byte data)
{
    public static Value ZERO = Value.of(0);

    public static Value of(byte b)
    {
        return new Value(b);
    }

    public static Value of(int i)
    {
        return new Value((byte) i);
    }

    public static Value ofHex(String hex)
    {
        return new Value((byte) Integer.parseInt(hex, 16));
    }

    public static Value ofBits(String bits)
    {
        return new Value((byte) Integer.parseInt(bits, 2));
    }

    public Value and(Value other)
    {
        return Value.of(data & other.data);
    }

    public Value or(Value other)
    {
        return Value.of(data | other.data);
    }

    public Value xor(Value other)
    {
        return Value.of(data ^ other.data);
    }

    public Value not()
    {
        return Value.of(~data);
    }

    public Value increment()
    {
        return Value.of(data + 1);
    }

    public Value decrement()
    {
        return Value.of(data - 1);
    }

    public Value set(int position)
    {
        return or(Value.of(1 << position));
    }

    public Value clear(int position)
    {
        return and(Value.of(~(1 << position)));
    }

    public boolean isNegative()
    {
        return data < 0;
    }

    public boolean isZero()
    {
        return data == 0;
    }

    public boolean isSet(int position)
    {
        if (position < 0 || position > 7) {
            throw new IllegalArgumentException("Position must be 0-7 to reference the bits of a single byte");
        }
        return !and(Value.of(1 << position)).equals(ZERO);
    }

    public Value plus(Value other)
    {
        return Value.of(data + Byte.toUnsignedInt(other.data()));
    }

    @Override
    public String toString()
    {
        return "0x%02X".formatted(data);
    }
}
