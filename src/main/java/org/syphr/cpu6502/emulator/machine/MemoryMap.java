package org.syphr.cpu6502.emulator.machine;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class MemoryMap implements AddressHandler
{
    private final Map<Address, Value> map;

    public MemoryMap(Map<Address, Value> map)
    {
        this.map = new TreeMap<>(map);
    }

    @Override
    public Value read(Address address)
    {
        Value value = map.get(address);
        value = value == null ? Value.ZERO : value;
        log.info("Read {} from {}", value, address);
        return value;
    }

    @Override
    public void write(Address address, Value value)
    {
        map.put(address, value);
        log.info("Wrote {} to {}", value, address);
    }
}
