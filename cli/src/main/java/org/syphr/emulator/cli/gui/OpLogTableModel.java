package org.syphr.emulator.cli.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.syphr.emulator.cpu.CPUState;
import org.syphr.emulator.cpu.Flags;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpLogTableModel extends AbstractTableModel
{
    @RequiredArgsConstructor
    @Getter
    enum Column
    {
        PROGRAM_COUNTER("PC"), STATUS("NV-BDIZC"), ACCUMULATOR("A"), X("X"), Y("Y"), STACK_POINTER("SP");

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

    private final List<CPUState> states = new ArrayList<>();

    @Override
    public int getRowCount()
    {
        return states.size();
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
        CPUState state = states.get(rowIndex);

        return switch (Column.fromIndex(columnIndex)) {
            case PROGRAM_COUNTER -> state.programCounter();
            case STATUS -> flagsAsBits(state.flags());
            case ACCUMULATOR -> state.accumulator();
            case X -> state.x();
            case Y -> state.y();
            case STACK_POINTER -> state.stackPointer();
        };
    }

    public void addState(CPUState state)
    {
        states.add(state);
        int newRow = states.size() - 1;
        fireTableRowsInserted(newRow, newRow);
    }

    private String flagsAsBits(Flags flags)
    {
        return Stream.<Supplier<Boolean>>of(flags::negative, flags::overflow)
                     .map(Supplier::get)
                     .map(this::booleanToBit)
                     .collect(Collectors.joining());
    }

    private String booleanToBit(boolean b)
    {
        return b ? "1" : "0";
    }
}
