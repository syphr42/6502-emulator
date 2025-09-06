package org.syphr.emulator.cli;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.syphr.emulator.cpu.Address;

@Component
public class AddressConverter implements Converter<String, Address>
{
    @Override
    public Address convert(String source)
    {
        return Address.of(Integer.decode(source));
    }
}
