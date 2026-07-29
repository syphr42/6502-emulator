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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.syphr.emulator.cpu.Address;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoryMapTest
{
    @Test
    void constructor_EmptySegmentList_Throws()
    {
        // when / then
        assertThatThrownBy(() -> new MemoryMap(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Memory segment list is empty");
    }

    @Test
    void constructor_NonOverlappingAdjacentSegments_Succeeds()
    {
        // given
        var s1 = new RAM(Address.of(0x0000), Address.of(0x00FF));
        var s2 = new RAM(Address.of(0x0100), Address.of(0x01FF));

        // when
        var map = new MemoryMap(List.of(s2, s1));

        // then
        assertThat(map.segments()).hasSize(2).contains(s1, s2);
    }

    @Test
    void constructor_OverlappingSameBoundary_Throws()
    {
        // given
        var s1 = new RAM(Address.of(0x0100), Address.of(0x01FF));
        var s2 = new RAM(Address.of(0x01FF), Address.of(0x02FF));

        // when / then
        assertThatThrownBy(() -> new MemoryMap(List.of(s1, s2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Memory segments cannot overlap");
    }

    @Test
    void constructor_OverlappingIdenticalSingleAddress_Throws()
    {
        // given
        var a = Address.of(0x1000);
        var s1 = new RAM(a, a);
        var s2 = new RAM(a, a);

        // when / then
        assertThatThrownBy(() -> new MemoryMap(List.of(s1, s2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 5})
    void constructor_MultipleNonOverlappingSegments_Succeeds(int segmentCount)
    {
        // given
        var segments = new RAM[segmentCount];
        for (int i = 0; i < segmentCount; i++) {
            int start = i * 0x0100;
            int end = start + 0x00FF;
            segments[i] = new RAM(Address.of(start), Address.of(end));
        }

        // when
        var map = new MemoryMap(List.of(segments));

        // then
        assertThat(map.segments()).hasSize(segmentCount);
    }

    @Test
    void constructor_OverlappingFirstTwoWithinMultipleSegments_Throws()
    {
        // given
        var s1 = new RAM(Address.of(0x0000), Address.of(0x00FF));
        var s2 = new RAM(Address.of(0x00F0), Address.of(0x01FF));
        var s3 = new RAM(Address.of(0x0200), Address.of(0x02FF));

        // when / then
        assertThatThrownBy(() -> new MemoryMap(List.of(s1, s2, s3)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_OverlappingLaterWithinMultipleSegments_Throws()
    {
        // given
        var s1 = new RAM(Address.of(0x0000), Address.of(0x00FF));
        var s2 = new RAM(Address.of(0x0100), Address.of(0x01FF));
        var s3 = new RAM(Address.of(0x0150), Address.of(0x0250));
        var s4 = new RAM(Address.of(0x0200), Address.of(0x02FF));

        // when / then
        assertThatThrownBy(() -> new MemoryMap(List.of(s1, s2, s3, s4)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_OverlappingMultipleSegmentsOutOfOrder_Throws()
    {
        // given
        var s1 = new RAM(Address.of(0x0150), Address.of(0x0250));
        var s2 = new RAM(Address.of(0x0200), Address.of(0x02FF));
        var s3 = new RAM(Address.of(0x0000), Address.of(0x00FF));
        var s4 = new RAM(Address.of(0x0100), Address.of(0x01FF));

        // when / then
        assertThatThrownBy(() -> new MemoryMap(List.of(s1, s2, s3, s4)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
