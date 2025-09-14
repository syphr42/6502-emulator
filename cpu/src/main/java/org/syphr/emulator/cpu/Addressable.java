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

import lombok.RequiredArgsConstructor;

public interface Addressable extends Reader, Writer
{
    static Addressable of(Reader reader, Writer writer)
    {
        return new DelegatingAddressable(reader, writer);
    }

    @RequiredArgsConstructor
    class DelegatingAddressable implements Addressable
    {
        private final Reader reader;
        private final Writer writer;

        @Override
        public Value read(Address address)
        {
            return reader.read(address);
        }

        @Override
        public void write(Address address, Value value)
        {
            writer.write(address, value);
        }
    }
}
