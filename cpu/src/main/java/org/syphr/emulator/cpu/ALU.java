/*
 * Copyright © 2025-2026 Gregory P. Moyer
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
import org.syphr.emulator.common.Register;
import org.syphr.emulator.common.Value;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
class ALU
{
    private final StatusRegister status;

    public void addWithCarry(Register register, Value value)
    {
        if (status.decimal()) {
            addWithCarryDecimalMode(register, value, status);
        } else {
            addWithCarryBinaryMode(register, value, status);
        }
    }

    private void addWithCarryBinaryMode(Register register, Value value, StatusRegister s)
    {
        byte r = register.value().data();
        byte m = value.data();
        byte c = (byte) (s.carry() ? 0x01 : 0x00);

        int unsignedResult = Byte.toUnsignedInt(r) + Byte.toUnsignedInt(m) + c;
        boolean carry = unsignedResult > 255;

        int signedResult = r + m + c;
        boolean overflow = signedResult > Byte.MAX_VALUE || signedResult < Byte.MIN_VALUE;

        Value result = Value.of(unsignedResult);
        register.load(result);
        s.setNegative(result.isNegative()).setOverflow(overflow).setZero(result.isZero()).setCarry(carry);
    }

    private void addWithCarryDecimalMode(Register register, Value value, StatusRegister s)
    {
        byte r = register.value().data();
        byte m = value.data();
        byte c = (byte) (s.carry() ? 0x01 : 0x00);

        int a = Byte.toUnsignedInt(r);
        int b = Byte.toUnsignedInt(m);
        int al = (a & 0x0F) + (b & 0x0F) + c;
        al = al >= 0x0A ? ((al + 0x06) & 0x0F) + 0x10 : al;

        int unsignedResult = (a & 0xF0) + (b & 0xF0) + al;
        unsignedResult = unsignedResult >= 0xA0 ? unsignedResult + 0x60 : unsignedResult;
        boolean carry = unsignedResult >= 0x100;

        // calculate overflow flag
        var acc = Register.with(register.value().and(Value.of(0x0F))); // RLow
        var mLow = value.and(Value.of(0x0F)); // MLow
        var tmpStatus = StatusRegister.of(s.flags()); // copy of status
        addWithCarryBinaryMode(acc, mLow, tmpStatus); // acc = RLow + MLow
        compare(acc, Value.of(0x0A), tmpStatus); // carry set if acc >= 0A
        if (tmpStatus.carry()) {
            acc.load(Value.of((Byte.toUnsignedInt(acc.value().data()) + 0x06) & 0x0F)); // acc += 6 (low bits only)
        }
        acc.load(acc.value().or(register.value().and(Value.of(0xF0)))); // acc | RHigh
        if (tmpStatus.carry()) {
            addWithCarryBinaryMode(acc, Value.of((m & 0xF0) | 0x0F), tmpStatus);
        } else {
            addWithCarryBinaryMode(acc, Value.of(m & 0xF0), tmpStatus);
        }
        boolean overflow = tmpStatus.overflow();

        Value result = Value.of(unsignedResult);
        register.load(result);
        s.setNegative(result.isNegative()).setOverflow(overflow).setZero(result.isZero()).setCarry(carry);
    }

    public void subtractWithCarry(Register register, Value value)
    {
        if (status.decimal()) {
            subtractWithCarryDecimalMode(register, value);
        } else {
            subtractWithCarryBinaryMode(register, value);
        }
    }

    private void subtractWithCarryBinaryMode(Register register, Value value)
    {
        byte r = register.value().data();
        byte m = value.data();
        byte c = (byte) (status.carry() ? 0x00 : 0x01);

        int unsignedResult = Byte.toUnsignedInt(r) - Byte.toUnsignedInt(m) - Byte.toUnsignedInt(c);
        boolean carry = unsignedResult >= 0;

        int signedResult = r - m - c;
        boolean overflow = signedResult > Byte.MAX_VALUE || signedResult < Byte.MIN_VALUE;

        Value result = Value.of(unsignedResult);
        register.load(result);
        status.setNegative(result.isNegative()).setOverflow(overflow).setZero(result.isZero()).setCarry(carry);
    }

    private void subtractWithCarryDecimalMode(Register register, Value value)
    {
        byte r = register.value().data();
        byte m = value.data();
        byte c = (byte) (status.carry() ? 0x00 : 0x01);

        int a = Byte.toUnsignedInt(r);
        int b = Byte.toUnsignedInt(m);
        int al = (a & 0x0F) - (b & 0x0F) - c;

        int unsignedResult = a - b - c;
        unsignedResult = unsignedResult < 0x00 ? unsignedResult - 0x60 : unsignedResult;
        unsignedResult = al < 0x00 ? unsignedResult - 0x06 : unsignedResult;
        boolean carry = (a - b - c) >= 0;

        int signedResult = r - m - c;
        boolean overflow = signedResult > Byte.MAX_VALUE || signedResult < Byte.MIN_VALUE;

        Value result = Value.of(unsignedResult);
        register.load(result);
        status.setNegative(result.isNegative()).setOverflow(overflow).setZero(result.isZero()).setCarry(carry);
    }

    public void compare(Register register, Value value)
    {
        compare(register, value, status);
    }

    private void compare(Register register, Value value, StatusRegister s)
    {
        int compare = Byte.compareUnsigned(register.value().data(), value.data());
        s.setNegative((compare & 0x80) != 0).setZero(compare == 0).setCarry(compare >= 0);
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
