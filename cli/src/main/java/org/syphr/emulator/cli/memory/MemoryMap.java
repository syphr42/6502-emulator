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
package org.syphr.emulator.cli.memory;

import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.Addressable;
import org.syphr.emulator.cpu.Operation;
import org.syphr.emulator.cpu.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MemoryMap implements Addressable
{
    private final List<Segment> segments;

    public static MemoryMap of(Segment... segments)
    {
        return new MemoryMap(List.of(segments));
    }

    public static MemoryMap of(Address start, Path rom) throws IOException
    {
        Map<Address, Value> map = new TreeMap<>();
        byte[] bytes = Files.readAllBytes(rom);

        var address = start;
        for (byte b : bytes) {
            map.put(address, Value.of(b));
            address = address.increment();
        }

        return of(new RAM(Address.of(0x0000), Address.of(0xFFFF), map));
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

        return of(new RAM(Address.of(0x0000), Address.of(0xFFFF), map));
    }

    public MemoryMap(List<Segment> segments)
    {
        this.segments = List.copyOf(segments);
    }

    @Override
    public Value read(Address address)
    {
        return segments.stream()
                       .filter(s -> s.contains(address))
                       .findFirst()
                       .orElseThrow(() -> new IllegalArgumentException("No memory segment contains address " + address))
                       .read(address);
    }

    @Override
    public void write(Address address, Value value)
    {
        segments.stream()
                .filter(s -> s.contains(address))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No memory segment contains address " + address))
                .write(address, value);
    }
}
