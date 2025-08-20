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

    @Test
    void execute_ADC_Absolute()
    {
        // given
        accumulator.store(Value.of(0x01));

        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        var op = Operation.adc(target);

        // when
        cpu.execute(op);

        // then
        assertThat(accumulator.value()).isEqualTo(Value.of(0x11));
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
    void execute_ADC_Immediate(String acc,
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
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .overflow(isOverflow)
                                                                  .zero(isZero)
                                                                  .carry(isCarry)
                                                                  .build()));
    }

    @Test
    void execute_AND_Absolute()
    {
        // given
        accumulator.store(Value.of(0b1100));

        Address target = Address.of(0x1234);
        Value value = Value.of(0b0101);
        when(reader.read(target)).thenReturn(value);

        var op = Operation.and(target);

        // when
        cpu.execute(op);

        // then
        assertThat(accumulator.value()).isEqualTo(Value.of(0b0100));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00, false, true",
                "00, FF, 00, false, true",
                "FF, FF, FF, true, false",
                "FF, 00, 00, false, true",
                "04, 07, 04, false, false"})
    void execute_AND_Immediate(String acc, String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));
        var op = Operation.and(Value.ofHex(input));

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"00000000, 00000000, false, true, false",
                "00000001, 00000010, false, false, false",
                "10000000, 00000000, false, true, true",
                "10101010, 01010100, false, false, true",
                "01010101, 10101010, true, false, false",
                "11000000, 10000000, true, false, true"})
    void execute_ASL_Accumulator(String acc, String expected, boolean isNegative, boolean isZero, boolean isCarry)
    {
        // given
        accumulator.store(Value.ofBits(acc));
        var op = Operation.asl();

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofBits(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .carry(isCarry)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 1, 0001",
                "0001, 10, 0, 0011",
                "00FE, 02, 0, 0100",
                "0000, FF, 0, 00FF",
                "FFFF, 01, 0, 0000"})
    void execute_BCC_ProgramCounterRelative(String start, String displacement, int carry, String expected)
    {
        // given
        cpu.execute(Operation.jmp(Address.ofHex(start)));
        cpu.setFlags(Flags.builder().carry(carry != 0).build());

        var op = Operation.bcc(Value.ofHex(displacement));

        // when
        cpu.execute(op);

        // then
        assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expected));
    }

    @ParameterizedTest
    @CsvSource({"00, FF, true, false",
                "01, 00, false, true",
                "FF, FE, true, false"})
    void execute_DEC_Accumulator(String acc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));
        var op = Operation.dec();

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"FF, 00, false, true",
                "00, 01, false, false",
                "FE, FF, true, false"})
    void execute_INC_Accumulator(String acc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));
        var op = Operation.inc();

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @Test
    void execute_JMP_Absolute()
    {
        // given
        Address address = Address.of(0x1234);
        var op = Operation.jmp(address);

        // when
        cpu.execute(op);

        // then
        assertThat(cpu.getProgramCounter()).isEqualTo(address);
    }

    @Test
    void execute_JSR_Absolute()
    {
        // given
        var start = Address.of(0x1234);
        cpu.execute(Operation.jmp(start));

        var target = Address.of(0x3000);
        var op = Operation.jsr(target);

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(target),
                  () -> assertThat(Address.of(stack.pop(), stack.pop())).isEqualTo(start));
    }

    @Test
    void execute_LDA_Absolute()
    {
        // given
        accumulator.store(Value.ZERO);

        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        var op = Operation.lda(target);

        // when
        cpu.execute(op);

        // then
        assertThat(accumulator.value()).isEqualTo(value);
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true",
                "01, 01, false, false",
                "FF, FF, true, false"})
    void execute_LDA_Immediate(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ZERO);
        var op = Operation.lda(Value.ofHex(input));

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @Test
    void execute_ORA_Absolute()
    {
        // given
        accumulator.store(Value.of(0b1100));

        Address target = Address.of(0x1234);
        Value value = Value.of(0b0101);
        when(reader.read(target)).thenReturn(value);

        var op = Operation.ora(target);

        // when
        cpu.execute(op);

        // then
        assertThat(accumulator.value()).isEqualTo(Value.of(0b1101));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00, false, true",
                "00, FF, FF, true, false",
                "FF, FF, FF, true, false",
                "FF, 00, FF, true, false",
                "04, 07, 07, false, false"})
    void execute_ORA(String acc, String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));
        var op = Operation.ora(Value.ofHex(input));

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_PHA_Stack(String acc)
    {
        // given
        accumulator.store(Value.ofHex(acc));
        var op = Operation.pha();

        // when
        cpu.execute(op);

        // then
        assertThat(stack.pop()).isEqualTo(Value.ofHex(acc));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true",
                "01, 01, false, false",
                "FF, FF, true, false"})
    void execute_PLA_Stack(String initStack, String expected, boolean isNegative, boolean isZero)
    {
        // given
        stack.push(Value.ofHex(initStack));
        var op = Operation.pla();

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(Flags.builder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @Test
    void execute_RTS_Stack()
    {
        // given
        var start = Address.of(0x1234);
        cpu.execute(Operation.jmp(start));

        var target = Address.of(0x3000);
        cpu.execute(Operation.jsr(target));

        var op = Operation.rts();

        // when
        cpu.execute(op);

        // then
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(start),
                  () -> assertThat(stack.isEmpty()).isTrue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_STA_Absolute(String acc)
    {
        // given
        accumulator.store(Value.ofHex(acc));
        var target = Address.of(0x1234);
        var op = Operation.sta(target);

        // when
        cpu.execute(op);

        // then
        verify(writer).write(target, Value.ofHex(acc));
    }
}
