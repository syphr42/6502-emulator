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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ProgramManager
{
    private final Reader reader;

    @Getter
    private Address programCounter = Address.of(0x00FF);

    public void read()
    {
        log.info("Performing throwaway read at program counter");
        reader.read(programCounter);
    }

    public Value nextValue()
    {
        Value value = reader.read(programCounter);
        programCounter = programCounter.increment();

        return value;
    }

    public Address nextAddress()
    {
        return Address.of(nextValue(), nextValue());
    }

    public void setProgramCounter(Address address)
    {
        programCounter = address;
        log.info("Program counter set to {}", address);
    }
}
