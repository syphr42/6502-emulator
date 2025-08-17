package org.syphr.cpu6502.emulator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return (Address address, byte data) -> System.out.println("Write " + data + " to " + address);
    }
}
