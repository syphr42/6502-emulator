package org.syphr.emulator.cli.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
@RequiredArgsConstructor
public class ConversionConfig
{
    @Bean
    public ConversionService createConversionService()
    {
        var conversionService = new ApplicationConversionService();
        conversionService.addConverter(new AddressConverter());
        conversionService.addConverter(new PathConverter());

        return conversionService;
    }
}
