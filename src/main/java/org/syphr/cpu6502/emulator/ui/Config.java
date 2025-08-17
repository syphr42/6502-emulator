package org.syphr.cpu6502.emulator.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.syphr.cpu6502.emulator.machine.Address;
import org.syphr.cpu6502.emulator.machine.AddressHandler;
import org.syphr.cpu6502.emulator.machine.CPU;
import org.syphr.cpu6502.emulator.machine.Reader;
import org.syphr.cpu6502.emulator.machine.Value;
import org.syphr.cpu6502.emulator.machine.Writer;

@Configuration
public class Config
{
    @Bean
    CPU createCPU(AddressHandler addressHandler)
    {
        return new CPU(addressHandler, addressHandler);
    }

    @Bean
    AddressHandler createAddressHandler(Reader reader, Writer writer)
    {
        return AddressHandler.of(reader, writer);
    }

    @Bean
    Reader createReader()
    {
        return (Address address) -> {
            Value data = Value.of(0);
            System.out.println("Read " + data + " from " + address);
            return data;
        };
    }

    @Bean
    Writer createWriter()
    {
        return (Address address, Value data) -> System.out.println("Wrote " + data + " to " + address);
    }
}
