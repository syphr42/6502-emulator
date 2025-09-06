package org.syphr.emulator.cli;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class PathConverter implements Converter<String, Path>
{
    @Override
    public Path convert(String source)
    {
        return Path.of(source);
    }
}
