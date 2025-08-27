package org.syphr.cpu6502.emulator.ui;

import org.syphr.cpu6502.emulator.machine.Address;
import org.syphr.cpu6502.emulator.machine.AddressHandler;
import org.syphr.cpu6502.emulator.machine.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class MemoryMap implements AddressHandler
{
    private final Map<Address, Value> map;

    public static MemoryMap of(Address start, Path rom) throws IOException
    {
        Map<Address, Value> map = new TreeMap<>();
        byte[] bytes = Files.readAllBytes(rom);

        for (int i = 0; i < bytes.length; i++) {
            map.put(Address.of(Short.toUnsignedInt(start.data()) + i), Value.of(bytes[i]));
        }

        return new MemoryMap(map);
    }

    public MemoryMap(Map<Address, Value> map)
    {
        this.map = new TreeMap<>(map);
    }

    @Override
    public Value read(Address address)
    {
        return map.getOrDefault(address, Value.ZERO);
    }

    @Override
    public void write(Address address, Value value)
    {
        map.put(address, value);
    }
}
