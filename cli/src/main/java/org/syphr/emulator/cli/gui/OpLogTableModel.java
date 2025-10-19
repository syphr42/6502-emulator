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

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class OpLogTableModel extends AbstractTableModel
{
    @RequiredArgsConstructor
    @Getter
    public enum Column
    {
        OP("Completed Operation"),
        START_CYCLE("Clock Start"),
        END_CYCLE("Clock End");

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

    private final List<OperationEvent> events = new ArrayList<>();

    @Override
    public int getRowCount()
    {
        return events.size();
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
        OperationEvent event = events.get(rowIndex);

        return switch (Column.fromIndex(columnIndex)) {
            case START_CYCLE -> event.startCycle();
            case END_CYCLE -> event.endCycle();
            case OP -> event.op();
        };
    }

    public void addEvent(OperationEvent event)
    {
        int newRowIndex = getRowCount();
        events.add(event);
        fireTableRowsInserted(newRowIndex, newRowIndex);
    }

    public void clear()
    {
        int rowCount = getRowCount();
        if (rowCount == 0) {
            return;
        }

        int lastRowIndex = rowCount - 1;
        events.clear();
        fireTableRowsDeleted(0, lastRowIndex);
    }
}
