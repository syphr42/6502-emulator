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
package org.syphr.emulator.cli.gui;

import lombok.Getter;
import org.syphr.emulator.cli.memory.MemoryMap;
import org.syphr.emulator.cli.memory.RAM;
import org.syphr.emulator.cli.memory.ROM;
import org.syphr.emulator.cli.memory.Segment;
import org.syphr.emulator.common.Value;
import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.Addressable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressTableModel extends AbstractTableModel
{
    private static final int MAX_ADDRESS = Address.MAX.toUnsignedInt();
    private static final int ADDRESSES_PER_ROW = 16;
    private static final int ROW_COUNT = (MAX_ADDRESS + ADDRESSES_PER_ROW) / ADDRESSES_PER_ROW;

    private final Map<Address, Value> map = new HashMap<>();

    @Getter
    private Addressable memoryMap = new MemoryMap(List.of(new RAM(Address.MIN, Address.MAX)));

    @Override
    public int getRowCount()
    {
        return ROW_COUNT;
    }

    @Override
    public int getColumnCount()
    {
        return ADDRESSES_PER_ROW + 1;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return columnIndex == 0 ? "" : "%01X".formatted(columnIndex - 1);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        return columnIndex == 0 ? Address.class : Value.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Address address = toAddress(rowIndex, columnIndex);

        if (columnIndex == 0) {
            return address;
        }

        return map.getOrDefault(address, Value.ZERO);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex)
    {
        if (columnIndex == 0) {
            throw new UnsupportedOperationException("Cannot change the address column");
        }

        if (value instanceof Value v) {
            map.put(toAddress(rowIndex, columnIndex), v);
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        throw new IllegalArgumentException("Invalid object type: " + value.getClass());
    }

    public void updateAddress(Address address, Value value)
    {
        map.put(address, value);

        int rowIndex = address.data() / ADDRESSES_PER_ROW;
        int columnIndex = (address.data() % ADDRESSES_PER_ROW) + 1;
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void loadMemoryMap(MemoryMap memoryMap)
    {
        clear();

        for (Segment segment : memoryMap.getSegments()) {
            if (segment instanceof ROM rom) {
                for (Address address = rom.getStart(); !address.equals(rom.getEnd()); address = address.increment()) {
                    updateAddress(address, rom.read(address));
                }
            }
        }

        this.memoryMap = new Addressable()
        {
            @Override
            public Value read(Address address)
            {
                Value result = memoryMap.read(address);
                SwingUtilities.invokeLater(() -> updateAddress(address, result));
                return result;
            }

            @Override
            public void write(Address address, Value value)
            {
                memoryMap.write(address, value);
                SwingUtilities.invokeLater(() -> updateAddress(address, value));
            }
        };
    }

    private Address toAddress(int rowIndex, int columnIndex)
    {
        var baseAddress = Address.of(ADDRESSES_PER_ROW * rowIndex);
        if (columnIndex == 0) {
            return baseAddress;
        }

        return baseAddress.plus(columnIndex - 1);
    }

    private void clear()
    {
        map.clear();
        fireTableDataChanged();
    }
}
