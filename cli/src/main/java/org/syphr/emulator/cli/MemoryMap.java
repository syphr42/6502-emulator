/*
 * Copyright © 2025 Gregory P. Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.syphr.emulator.cli;

import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.AddressHandler;
import org.syphr.emulator.cpu.Operation;
import org.syphr.emulator.cpu.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MemoryMap implements AddressHandler
{
    private final Map<Address, Value> map;

    public static MemoryMap of(Address start, Path rom) throws IOException
    {
        Map<Address, Value> map = new TreeMap<>();
        byte[] bytes = Files.readAllBytes(rom);

        var address = start;
        for (byte b : bytes) {
            map.put(address, Value.of(b));
            address = address.increment();
        }

        return new MemoryMap(map);
    }

    public static MemoryMap of(Address start, List<Operation> operations)
    {
        Map<Address, Value> map = new TreeMap<>();

        var address = start;
        for (Operation op : operations) {
            List<Value> values = Operation.toValues(op);
            for (Value value : values) {
                map.put(address, value);
                address = address.increment();
            }
        }

        map.put(Address.RESET, start.low());
        map.put(Address.RESET.increment(), start.high());

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
