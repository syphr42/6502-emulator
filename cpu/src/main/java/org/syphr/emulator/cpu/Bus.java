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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.syphr.emulator.common.Value;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
class Bus
{
    private volatile Address address = Address.MIN;
    private volatile Value data = Value.ZERO;
    private volatile BusAction lastAction = BusAction.READ;

    public void update(Address address, Value data, BusAction action)
    {
        this.address = address;
        this.data = data;
        this.lastAction = action;
    }
}
