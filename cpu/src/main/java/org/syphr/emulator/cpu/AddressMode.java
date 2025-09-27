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

import org.syphr.emulator.common.Value;

public sealed interface AddressMode
{
    // @formatter:off
    record Absolute(Address address) implements AddressMode {}
    static Absolute absolute(Address address) { return new Absolute(address); }

    record AbsoluteIndexedXIndirect(Address address) implements AddressMode {}
    static AbsoluteIndexedXIndirect absoluteXIndirect(Address address) { return new AbsoluteIndexedXIndirect(address); }

    record AbsoluteIndexedX(Address address) implements AddressMode {}
    static AbsoluteIndexedX absoluteX(Address address) { return new AbsoluteIndexedX(address); }

    record AbsoluteIndexedY(Address address) implements AddressMode {}
    static AbsoluteIndexedY absoluteY(Address address) { return new AbsoluteIndexedY(address); }

    record AbsoluteIndirect(Address address) implements AddressMode {}
    static AbsoluteIndirect absoluteIndirect(Address address) { return new AbsoluteIndirect(address); }

    record Accumulator() implements AddressMode {}
    static Accumulator accumulator() { return new Accumulator(); }

    record Immediate(Value value) implements AddressMode {}
    static Immediate immediate(Value value) { return new Immediate(value); }

    record Implied() implements AddressMode {}
    static Implied implied() { return new Implied(); }

    record Relative(Value displacement) implements AddressMode {}
    static Relative relative(Value displacement) { return new Relative(displacement); }

    record Stack() implements AddressMode {}
    static Stack stack() { return new Stack(); }

    record ZeroPage(Value offset) implements AddressMode {}
    static ZeroPage zp(Value offset) { return new ZeroPage(offset); }

    record ZeroPageIndexedXIndirect(Value offset) implements AddressMode {}
    static ZeroPageIndexedXIndirect zpXIndirect(Value offset) { return new ZeroPageIndexedXIndirect(offset); }

    record ZeroPageIndexedX(Value offset) implements AddressMode {}
    static ZeroPageIndexedX zpX(Value offset) { return new ZeroPageIndexedX(offset); }

    record ZeroPageIndexedY(Value offset) implements AddressMode {}
    static ZeroPageIndexedY zpY(Value offset) { return new ZeroPageIndexedY(offset); }

    record ZeroPageIndirect(Value offset) implements AddressMode {}
    static ZeroPageIndirect zpIndirect(Value offset) { return new ZeroPageIndirect(offset); }

    record ZeroPageIndirectIndexedY(Value offset) implements AddressMode {}
    static ZeroPageIndirectIndexedY zpIndirectY(Value offset) { return new ZeroPageIndirectIndexedY(offset); }

    record ZeroPageRelative(ZeroPage zp, Relative relative) implements AddressMode {}
    static ZeroPageRelative zpRelative(ZeroPage zp, Relative relative) { return new ZeroPageRelative(zp, relative); }
    // @formatter:on
}
