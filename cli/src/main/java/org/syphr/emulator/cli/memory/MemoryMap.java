/*
 * Copyright © 2025-2026 Gregory P. Moyer
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

import org.syphr.emulator.common.Value;
import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.Addressable;
import org.syphr.emulator.cpu.Operation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public record MemoryMap(List<Segment> segments) implements Addressable
{
    public static MemoryMap of(Segment... segments)
    {
        return new MemoryMap(List.of(segments));
    }

    /**
     * Build a memory map using the given binary data file represented as read-only memory (ROM) and fill any remaining
     * address space with random-access memory (RAM).
     *
     * @param start ROM start address
     * @param bin   pointer to binary data file
     * @return complete 64k memory map with unused address space represented as RAM
     * @throws IOException if the binary file cannot be read
     */
    public static MemoryMap of(Address start, Path bin) throws IOException
    {
        return of(start, bin, false);
    }

    /**
     * Build a memory map using the given binary data file represented as read-only memory (ROM) or writable
     * random-access memory (RAM) and fill any remaining address space with RAM.
     *
     * @param start data start address
     * @param bin   pointer to binary data file
     * @return complete 64k memory map with unused address space represented as RAM
     * @throws IOException if the binary file cannot be read
     */
    public static MemoryMap of(Address start, Path bin, boolean writable) throws IOException
    {
        byte[] bytes = Files.readAllBytes(bin);

        if (bytes.length == 0) {
            throw new IllegalArgumentException("Binary file is empty");
        }

        Address end = start.plus(bytes.length - 1);
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("Binary file is too large to fit in addressable memory starting at " +
                                               start);
        }

        List<Value> values = new ArrayList<>();
        for (byte b : bytes) {
            values.add(Value.of(b));
        }

        Segment segment = writable ? new RAM(start, values) : new ROM(start, values);
        return fillRam(segment);
    }

    /**
     * Build a memory map using the given list of operations as ROM and fill any remaining address space with RAM.
     *
     * @param start      ROM start address
     * @param operations list of operations that will represent the ROM
     * @return complete 64k memory map with unused address space represented as RAM
     */
    public static MemoryMap of(Address start, List<Operation> operations)
    {
        List<Value> values = operations.stream().map(Operation::toValues).flatMap(Collection::stream).toList();
        return fillRam(new ROM(start, values), new Vectors(Address.MIN, start, Address.MIN));
    }

    /**
     * Build a 64k memory map filling all unused space with RAM.
     *
     * @param segments non-overlapping memory segments in ascending address order
     * @return a new memory map containing the given segments and all other addresses acting as RAM
     */
    private static MemoryMap fillRam(Segment... segments)
    {
        List<Segment> contiguous = new ArrayList<>();
        var next = Address.MIN;

        for (Segment segment : segments) {
            if (!next.equals(segment.getStart())) {
                contiguous.add(new RAM(next, segment.getStart().decrement()));
            }

            contiguous.add(segment);
            next = segment.getEnd().increment();
        }

        // if the space is all used, next should have wrapped around to the beginning
        if (!Address.MIN.equals(next)) {
            contiguous.add(new RAM(next, Address.MAX));
        }

        return new MemoryMap(contiguous);
    }

    public MemoryMap(List<Segment> segments)
    {
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Memory segment list is empty");
        }
        if (isOverlapping(segments)) {
            throw new IllegalArgumentException("Memory segments cannot overlap");
        }

        this.segments = List.copyOf(segments);
    }

    private boolean isOverlapping(List<Segment> segments)
    {
        List<Segment> sorted = segments.stream()
                                       .sorted(Comparator.comparing(Segment::getStart))
                                       .toList();

        for (int i = 0; i < sorted.size() - 1; i++) {
            Segment s1 = sorted.get(i);
            Segment s2 = sorted.get(i + 1);
            if (s1.getEnd().compareTo(s2.getStart()) >= 0) {
                return true;
            }
        }

        return false;
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
