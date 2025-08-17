package org.syphr.cpu6502.emulator;

import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.HexFormat;

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
        this.value = toByte(value);
    }

    private byte toByte(Value value)
    {
        return switch (value) {
            case Value.Binary b -> b.value();
            case Value.Decimal d -> (byte) d.value();
            case Value.Hex h -> (byte) HexFormat.fromHexDigits(h.value());
        };
    }
}
