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
package org.syphr.emulator.cpu;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.syphr.emulator.common.Value;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
class Stack
{
    private static final Value PAGE_ONE = Value.of(0x01);
    private static final int SIZE = 256;

    private final Reader reader;
    private final Writer writer;

    @ToString.Include
    private final Deque<Value> data = new ArrayDeque<>(SIZE);

    @Setter
    @ToString.Include
    private Value pointer = Value.ZERO;

    public Stack(Reader reader, Writer writer)
    {
        this.reader = reader;
        this.writer = writer;
    }

    public Address getPointer()
    {
        return Address.of(pointer, PAGE_ONE);
    }

    public void push(Value value)
    {
        if (isFull()) {
            log.warn("Stack is full; data being pushed will overwrite oldest");
            data.removeLast();
        }
        data.push(value);

        Address address = getPointer();

        writer.write(address, value);
        log.info("{} pushed to stack at {}", value, address);

        pointer = pointer.decrement();
    }

    public void pushAll(List<Value> values)
    {
        values.forEach(this::push);
    }

    public Value pop()
    {
        if (isEmpty()) {
            log.warn("Value requested from the stack, but no data has been pushed");
        } else {
            data.pop();
        }

        pointer = pointer.increment();

        Address address = getPointer();
        Value value = reader.read(address);
        log.info("{} popped from stack at {}", value, address);

        return value;
    }

    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    public boolean isFull()
    {
        return data.size() == SIZE;
    }

    public List<Value> getData()
    {
        return List.copyOf(data);
    }
}
