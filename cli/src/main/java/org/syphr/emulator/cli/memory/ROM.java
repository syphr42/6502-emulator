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
import org.syphr.emulator.cpu.Value;

import java.util.List;

public class ROM extends Segment
{
    private final List<Value> values;

    public ROM(Address start, List<Value> values)
    {
        super(start, start.plus(values.size() - 1));
        this.values = List.copyOf(values);
    }

    @Override
    public Value read(Address address)
    {
        validate(address);
        return values.get(address.toUnsignedInt() - getStart().toUnsignedInt());
    }

    @Override
    public void write(Address address, Value value)
    {
        throw new UnsupportedOperationException("ROM cannot be modified");
    }
}
