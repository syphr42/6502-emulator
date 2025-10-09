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
import lombok.RequiredArgsConstructor;
import org.syphr.emulator.cpu.CPUEvent.OperationEvent;
import org.syphr.emulator.cpu.Flags;
import org.syphr.emulator.cpu.OperationListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpLogTableModel extends AbstractTableModel implements OperationListener
{
    @RequiredArgsConstructor
    @Getter
    enum Column
    {
        CLOCK_CYCLE("Clock"),
        PROGRAM_COUNTER("PC"),
        OP("OP"),
        STATUS("NV-BDIZC"),
        ACCUMULATOR("A"),
        X("X"),
        Y("Y"),
        STACK_POINTER("SP");

        private final String displayName;

        static Column fromIndex(int index)
        {
            Column[] values = values();
            if (index < 0 || index >= values.length) {
                throw new IllegalArgumentException("Invalid column index: " + index);
            }

            return values[index];
        }
    }

    private final List<OperationEvent> opEvents = new ArrayList<>();

    @Override
    public int getRowCount()
    {
        return opEvents.size();
    }

    @Override
    public int getColumnCount()
    {
        return Column.values().length;
    }

    @Override
    public String getColumnName(int column)
    {
        return Column.fromIndex(column).getDisplayName();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        OperationEvent event = opEvents.get(rowIndex);

        return switch (Column.fromIndex(columnIndex)) {
            case CLOCK_CYCLE -> event.clockCycle();
            case PROGRAM_COUNTER -> event.state().programCounter();
            case OP -> event.op();
            case STATUS -> flagsAsBits(event.state().flags());
            case ACCUMULATOR -> event.state().accumulator();
            case X -> event.state().x();
            case Y -> event.state().y();
            case STACK_POINTER -> event.state().stackPointer();
        };
    }

    @Override
    public void operationCompleted(OperationEvent event)
    {
        SwingUtilities.invokeLater(() -> {
            opEvents.add(event);
            int newRow = opEvents.size() - 1;
            fireTableRowsInserted(newRow, newRow);
        });
    }

    private String flagsAsBits(Flags flags)
    {
        return Stream.<Supplier<Boolean>>of(flags::negative,
                                            flags::overflow,
                                            flags::user,
                                            flags::breakCommand,
                                            flags::decimal,
                                            flags::irqDisable,
                                            flags::zero,
                                            flags::carry)
                     .map(Supplier::get)
                     .map(this::booleanToBit)
                     .collect(Collectors.joining());
    }

    private String booleanToBit(boolean b)
    {
        return b ? "1" : "0";
    }
}
