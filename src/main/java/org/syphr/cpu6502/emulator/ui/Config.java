package org.syphr.cpu6502.emulator.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.syphr.cpu6502.emulator.machine.Address;
import org.syphr.cpu6502.emulator.machine.Reader;
import org.syphr.cpu6502.emulator.machine.Stack;
import org.syphr.cpu6502.emulator.machine.Value;
import org.syphr.cpu6502.emulator.machine.Writer;

@Configuration
public class Config
{
    @Bean
    public Stack createStack()
    {
        return new Stack(256);
    }

    @Bean
    Reader createReader()
    {
        return (Address address) -> {
            System.out.println("Read from " + address);
            return (byte) 0;
        };
    }

    @Bean
    Writer createWriter()
    {
        return (Address address, Value data) -> System.out.println("Write " + data + " to " + address);
    }
}
