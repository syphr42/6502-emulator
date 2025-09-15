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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.Addressable;

@RequiredArgsConstructor
public abstract class Segment implements Addressable
{
    @Getter
    private final Address start;
    @Getter
    private final Address end;

    public boolean contains(Address address)
    {
        return address.compareTo(start) >= 0 && address.compareTo(end) <= 0;
    }

    protected void validate(Address address)
    {
        if (!contains(address)) {
            throw new IllegalArgumentException("Address " + address + " is outside the bounds of this segment");
        }
    }
}
