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
}
