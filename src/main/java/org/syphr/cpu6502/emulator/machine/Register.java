package org.syphr.cpu6502.emulator.machine;

import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@ToString
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
        this.value = value.toByte();
    }

    public byte value()
    {
        return value;
    }
}
