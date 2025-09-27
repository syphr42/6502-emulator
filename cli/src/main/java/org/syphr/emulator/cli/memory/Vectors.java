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

import java.util.List;

public class Vectors extends ROM
{
    public Vectors(Address nmi, Address reset, Address irq)
    {
        super(Address.NMI, List.of(nmi.low(), nmi.high(), reset.low(), reset.high(), irq.low(), irq.high()));
    }
}
