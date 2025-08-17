package org.syphr.cpu6502.emulator.machine;

import lombok.RequiredArgsConstructor;

public interface AddressHandler extends Reader, Writer
{
    static AddressHandler of(Reader reader, Writer writer)
    {
        return new AddressHandlerImpl(reader, writer);
    }

    @RequiredArgsConstructor
    public static class AddressHandlerImpl implements AddressHandler
    {
        private final Reader reader;
        private final Writer writer;

        @Override
        public Value read(Address address)
        {
            return reader.read(address);
        }

        @Override
        public void write(Address address, Value value)
        {
            writer.write(address, value);
        }
    }
}
