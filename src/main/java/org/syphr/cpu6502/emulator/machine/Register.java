package org.syphr.cpu6502.emulator.machine;

import org.springframework.stereotype.Component;

import java.util.HexFormat;

@Component
public class Register
{
    private byte value;

    public void decrement()
    {
        value--;
    }

    public void increment()
    {
        value++;
    }

    public void store(Value value)
    {
        this.value = value.data();
    }

    public Value value()
    {
        return Value.of(value);
    }

    @Override
    public String toString()
    {
        return Register.class.getSimpleName() + "[" +
                "0x" + HexFormat.of().toHexDigits(value) +
                ']';
    }
}
