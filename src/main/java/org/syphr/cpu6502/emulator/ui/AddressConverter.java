package org.syphr.cpu6502.emulator.ui;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.syphr.cpu6502.emulator.machine.Address;

@Component
public class AddressConverter implements Converter<String, Address>
{
    @Override
    public Address convert(String source)
    {
        return Address.of(Integer.decode(source));
    }
}
