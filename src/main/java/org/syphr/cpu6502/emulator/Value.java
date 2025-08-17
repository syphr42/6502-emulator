package org.syphr.cpu6502.emulator;

public sealed interface Value
{
    record Binary(byte value) implements Value {}

    record Decimal(int value) implements Value
    {
        public Decimal
        {
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("Decimal value must be between 0 and 255");
            }
        }
    }

    record Hex(String value) implements Value
    {
        public Hex
        {
            if (value.length() > 2) {
                throw new IllegalArgumentException("Hex value cannot be longer than a byte");
            }
        }
    }
}
