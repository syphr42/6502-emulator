package org.syphr.emulator.cli.gui;

import org.syphr.emulator.common.Value;
import org.syphr.emulator.cpu.Address;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.Map;

public class AddressTableModel extends AbstractTableModel
{
    private final Map<Address, Value> map = new HashMap<>();

    @Override
    public int getRowCount()
    {
        return 4096;
    }

    @Override
    public int getColumnCount()
    {
        return 17;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return "";
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

        int rowIndex = address.data() / 16;
        int columnIndex = (address.data() % 16) + 1;
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    private Address toAddress(int rowIndex, int columnIndex)
    {
        var baseAddress = Address.of(16 * rowIndex);
        if (columnIndex == 0) {
            return baseAddress;
        }

        return baseAddress.plus(columnIndex - 1);
    }
}
