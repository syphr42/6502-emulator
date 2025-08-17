package org.syphr.cpu6502.emulator.machine;

import java.util.HashMap;
import java.util.Map;

public class MemoryMap implements AddressHandler
{
    private final Map<Address, Value> map;

    public MemoryMap(Map<Address, Value> map)
    {
        this.map = new HashMap<>(map);
    }

    @Override
    public Value read(Address address)
    {
        Value value = map.get(address);
        return value == null ? Value.ZERO : value;
    }

    @Override
    public void write(Address address, Value value)
    {
        map.put(address, value);
    }
}
