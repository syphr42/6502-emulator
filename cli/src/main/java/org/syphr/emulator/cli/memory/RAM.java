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

import org.syphr.emulator.common.Value;
import org.syphr.emulator.cpu.Address;

import java.util.Map;
import java.util.TreeMap;

public class RAM extends Segment
{
    private final Map<Address, Value> data;

    public RAM(Address start, Address end)
    {
        super(start, end);
        this.data = new TreeMap<>();
    }

    @Override
    public Value read(Address address)
    {
        validate(address);
        return data.getOrDefault(address, Value.ZERO);
    }

    @Override
    public void write(Address address, Value value)
    {
        validate(address);
        data.put(address, value);
    }
}
