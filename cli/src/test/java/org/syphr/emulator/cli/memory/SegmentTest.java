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
package org.syphr.emulator.cli.memory;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.syphr.emulator.common.Value;
import org.syphr.emulator.cpu.Address;

import static org.junit.jupiter.api.Assertions.*;

class SegmentTest
{
    @Test
    void constructor_ValidRange_StoresBounds()
    {
        // given
        Address start = Address.of(0x8000);
        Address end = Address.of(0x80FF);

        // when
        TestSegment segment = new TestSegment(start, end);

        // then
        assertAll(
                () -> assertEquals(start, segment.getStart()),
                () -> assertEquals(end, segment.getEnd())
        );
    }

    @Test
    void constructor_SingleAddressRange_AllowsSingleByteSegment()
    {
        // given
        Address address = Address.of(0x1234);

        // when
        TestSegment segment = new TestSegment(address, address);

        // then
        assertAll(
                () -> assertEquals(address, segment.getStart()),
                () -> assertEquals(address, segment.getEnd()),
                () -> assertTrue(segment.contains(address))
        );
    }

    @Test
    void constructor_StartAfterEnd_ThrowsException()
    {
        // when
        RuntimeException result = assertThrows(RuntimeException.class,
                                               () -> new TestSegment(Address.of(0x8100), Address.of(0x8000)));

        // then
        assertEquals("Start address 0x8100 is greater than end address 0x8000", result.getMessage());
    }

    @Test
    void contains_AddressInsideBounds_ReturnsTrue()
    {
        // given
        TestSegment segment = new TestSegment(Address.of(0x8000), Address.of(0x80FF));

        // then
        assertAll(
                () -> assertTrue(segment.contains(Address.of(0x8000))),
                () -> assertTrue(segment.contains(Address.of(0x80FF))),
                () -> assertTrue(segment.contains(Address.of(0x8050)))
        );
    }

    @Test
    void contains_AddressOutsideBounds_ReturnsFalse()
    {
        // given
        TestSegment segment = new TestSegment(Address.of(0x8000), Address.of(0x80FF));

        // then
        assertAll(
                () -> assertFalse(segment.contains(Address.of(0x7FFF))),
                () -> assertFalse(segment.contains(Address.of(0x8100)))
        );
    }

    @Test
    void validate_AddressInsideBounds_DoesNotThrow()
    {
        // given
        TestSegment segment = new TestSegment(Address.of(0x9000), Address.of(0x90FF));

        // when / then
        segment.validate(Address.of(0x9000));
        segment.validate(Address.of(0x90FF));
        segment.validate(Address.of(0x9050));
    }

    @Test
    void validate_AddressOutsideBounds_ThrowsException()
    {
        // given
        TestSegment segment = new TestSegment(Address.of(0xA000), Address.of(0xA0FF));

        // when
        RuntimeException below = assertThrows(RuntimeException.class,
                                              () -> segment.validate(Address.of(0x9FFF)));
        RuntimeException above = assertThrows(RuntimeException.class,
                                              () -> segment.validate(Address.of(0xA100)));

        // then
        assertAll(
                () -> assertEquals("Address 0x9FFF is outside the bounds of this segment", below.getMessage()),
                () -> assertEquals("Address 0xA100 is outside the bounds of this segment", above.getMessage())
        );
    }

    @NullMarked
    private static class TestSegment extends Segment
    {
        private TestSegment(Address start, Address end)
        {
            super(start, end);
        }

        @Override
        public Value read(Address address)
        {
            return Value.ZERO;
        }

        @Override
        public void write(Address address, Value value)
        {
            // no-op
        }
    }
}
