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
package org.syphr.emulator.via;

import org.syphr.emulator.common.Register;
import org.syphr.emulator.common.Value;

public class Port
{
    private final Register input = new Register();
    private final Register output = new Register();
    private final Register dataDirection = new Register();

    public Value getInput()
    {
        return input.value();
    }

    public void setOutput(Value value)
    {
        output.load(value);
    }

    public Value getDataDirection()
    {
        return dataDirection.value();
    }

    public void setDataDirection(Value value)
    {
        dataDirection.load(value);
    }
}
