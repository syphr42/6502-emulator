package org.syphr.cpu6502.emulator.machine;

import java.util.Map;
import java.util.TreeMap;

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
        return map.getOrDefault(address, Value.ZERO);
    }

    @Override
    public void write(Address address, Value value)
    {
        map.put(address, value);
    }
}
