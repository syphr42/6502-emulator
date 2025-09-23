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

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
class ALU
{
    private final StatusRegister status;

    public void addWithCarry(Register register, Value value)
    {
        byte r = register.value().data();
        byte m = value.data();
        byte c = (byte) (status.carry() ? 0x01 : 0x00);

        int signedResult = r + m + c;
        boolean overflow = signedResult > Byte.MAX_VALUE || signedResult < Byte.MIN_VALUE;

        int unsignedResult = Byte.toUnsignedInt(r) + Byte.toUnsignedInt(m) + c;
        boolean carry = unsignedResult > 255;

        Value result = Value.of(unsignedResult);
        register.load(result);
        status.setNegative(result.isNegative()).setOverflow(overflow).setZero(result.isZero()).setCarry(carry);
    }

    public void subtractWithCarry(Register register, Value value)
    {
        byte r = register.value().data();
        byte v = value.data();
        byte c = (byte) (status.carry() ? 0x00 : 0x01);

        int signedResult = r - v - c;
        boolean overflow = signedResult > Byte.MAX_VALUE || signedResult < Byte.MIN_VALUE;

        int unsignedResult = Byte.toUnsignedInt(r) - Byte.toUnsignedInt(v) - Byte.toUnsignedInt(c);
        boolean carry = unsignedResult >= 0;

        Value result = Value.of(unsignedResult);
        register.load(result);
        status.setNegative(result.isNegative()).setOverflow(overflow).setZero(result.isZero()).setCarry(carry);
    }

    public void compare(Register register, Value value)
    {
        int compare = Byte.compareUnsigned(register.value().data(), value.data());
        status.setNegative((compare & 0x80) != 0).setZero(compare == 0).setCarry(compare >= 0);
    }

    public void load(Register register, Value value)
    {
        calculate(register, _ -> value);
    }

    public void calculate(Register register, Function<Value, Value> function)
    {
        update(register, r -> r.load(function.apply(r.value())));
    }

    public void update(Register register, Consumer<Register> action)
    {
        action.accept(register);
        status.setNegative(register.value().isNegative()).setZero(register.value().isZero());
    }

    public Value shiftLeft(Value value)
    {
        byte r = value.data();
        status.setCarry((r & 0x80) != 0);

        var result = Value.of(r << 1);
        status.setNegative(result.isNegative()).setZero(result.isZero());

        return result;
    }

    public Value shiftRight(Value value)
    {
        byte r = value.data();
        status.setCarry((r & 0x01) != 0);

        var result = Value.of(Byte.toUnsignedInt(r) >> 1);
        status.setNegative(result.isNegative()).setZero(result.isZero());

        return result;
    }

    public Value rotateLeft(Value value)
    {
        byte r = value.data();
        byte c = (byte) (status.carry() ? 0x01 : 0x00);
        status.setCarry((r & 0x80) != 0);

        var result = Value.of(c | (r << 1));
        status.setNegative(result.isNegative()).setZero(result.isZero());

        return result;
    }

    public Value rotateRight(Value value)
    {
        byte r = value.data();
        byte c = (byte) (status.carry() ? 0x80 : 0x00);
        status.setCarry((r & 0x01) != 0);

        var result = Value.of(c | (Byte.toUnsignedInt(r) >> 1));
        status.setNegative(result.isNegative()).setZero(result.isZero());

        return result;
    }

    public Value increment(Value value)
    {
        var result = value.increment();
        status.setNegative(result.isNegative()).setZero(result.isZero());

        return result;
    }

    public Value decrement(Value value)
    {
        var result = value.decrement();
        status.setNegative(result.isNegative()).setZero(result.isZero());

        return result;
    }
}
