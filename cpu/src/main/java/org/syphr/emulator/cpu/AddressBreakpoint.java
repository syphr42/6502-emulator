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
package org.syphr.emulator.cpu;

public record AddressBreakpoint(Address address, boolean onRead, boolean onWrite) implements Breakpoint
{
    @Override
    public boolean conditionMet(CPUState cpuState)
    {
        if (!cpuState.addressBus().equals(address)) {
            return false;
        }

        return switch (cpuState.lastBusAction()) {
            case READ -> onRead;
            case WRITE -> onWrite;
        };
    }
}
