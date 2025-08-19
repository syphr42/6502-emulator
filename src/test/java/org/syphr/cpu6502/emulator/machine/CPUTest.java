package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CPUTest
{
    Register accumulator;
    Stack stack;

    @Mock
    Reader reader;

    @Mock
    Writer writer;

    CPU cpu;

    @BeforeEach
    void beforeEach()
    {
        accumulator = new Register();
        stack = new Stack(256);

        cpu = new CPU(accumulator, stack, reader, writer);
    }

    @ParameterizedTest
    @CsvSource({"01, 01, 0, 02, false, false, false, false",
                "F0, 01, 0, F1, true, false, false, false",
                "01, FF, 0, 00, false, false, true, true",
                "02, FF, 0, 01, false, false, false, true",
                "7F, 01, 0, 80, true, true, false, false",
                "FF, FF, 0, FE, true, false, false, true",
                "80, FF, 0, 7F, false, true, false, true",
                "3F, 40, 1, 80, true, true, false, false"})
    void execute_ADC(String acc,
                     String input,
                     int carry,
                     String expected,
                     boolean isNegative,
                     boolean isOverflow,
                     boolean isZero,
                     boolean isCarry)
    {
        // given
        accumulator.store(Value.ofHex(acc));
        var op = Operation.adc(Value.ofHex(input));
        cpu.setFlags(Flags.builder().carry(carry != 0).build());

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value().data()).isEqualTo((byte) Integer.parseInt(expected, 16)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .overflow(isOverflow)
                                                                  .zero(isZero)
                                                                  .carry(isCarry)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00, false, true",
                "00, FF, 00, false, true",
                "FF, FF, FF, true, false",
                "FF, 00, 00, false, true",
                "04, 07, 04, false, false"})
    void execute_AND(String initAcc, String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(initAcc));
        var op = Operation.and(Value.ofHex(input));

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value().data()).isEqualTo((byte) Integer.parseInt(expected, 16)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"00, FF, true, false",
                "01, 00, false, true",
                "FF, FE, true, false"})
    void execute_DEC(String initAcc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(initAcc));
        var op = Operation.dec();

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value().data()).isEqualTo((byte) Integer.parseInt(expected, 16)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"FF, 00, false, true",
                "00, 01, false, false",
                "FE, FF, true, false"})
    void execute_INC(String initAcc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(initAcc));
        var op = Operation.inc();

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value().data()).isEqualTo((byte) Integer.parseInt(expected, 16)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true",
                "01, 01, false, false",
                "FF, FF, true, false"})
    void execute_LDA(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ZERO);
        var op = Operation.lda(Value.ofHex(input));

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value().data()).isEqualTo((byte) Integer.parseInt(expected, 16)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00, false, true",
                "00, FF, FF, true, false",
                "FF, FF, FF, true, false",
                "FF, 00, FF, true, false",
                "04, 07, 07, false, false"})
    void execute_ORA(String initAcc, String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(initAcc));
        var op = Operation.ora(Value.ofHex(input));

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value().data()).isEqualTo((byte) Integer.parseInt(expected, 16)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_PHA(String initAcc)
    {
        // given
        accumulator.store(Value.ofHex(initAcc));
        var op = Operation.pha();

        // when
        cpu.execute(op);

        // then
        assertThat(stack.pop()).isEqualTo(Value.ofHex(initAcc));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true",
                "01, 01, false, false",
                "FF, FF, true, false"})
    void execute_PLA(String initStack, String expected, boolean isNegative, boolean isZero)
    {
        // given
        stack.push(Value.ofHex(initStack));
        var op = Operation.pla();

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value().data()).isEqualTo((byte) Integer.parseInt(expected, 16)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_STA(String initAcc)
    {
        // given
        accumulator.store(Value.ofHex(initAcc));
        var addr = Address.ofHex("1234");
        var op = Operation.sta(addr);

        // when
        cpu.execute(op);

        // then
        verify(writer).write(addr, Value.ofHex(initAcc));
    }

    @Test
    void execute_AddressProvided_AddressResolves()
    {
        // given
        accumulator.store(Value.ofHex("00"));

        Address address = Address.ofHex("1234");
        var op = Operation.lda(address);

        Value returnedValue = Value.ofHex("FF");
        when(reader.read(address)).thenReturn(returnedValue);

        // when
        cpu.execute(op);

        // then
        assertThat(accumulator.value()).isEqualTo(returnedValue);
    }
}
