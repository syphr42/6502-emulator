package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.syphr.cpu6502.emulator.machine.AddressMode.*;
import static org.syphr.cpu6502.emulator.machine.Operation.*;

@ExtendWith(MockitoExtension.class)
class CPUTest
{
    Register accumulator;
    Stack stack;

    @Mock
    Clock clock;

    @Mock
    Reader reader;

    @Mock
    Writer writer;

    CPU cpu;

    @BeforeEach
    void beforeEach()
    {
        accumulator = new Register();
        stack = new Stack(256, clock);

        cpu = new CPU(accumulator, stack, clock, reader, writer);
    }

    private void setNextOp(Operation op)
    {
        Address pc = cpu.getProgramCounter();
        List<Value> values = toValues(op);
        for (int i = 0; i < values.size(); i++) {
            when(reader.read(pc.plus(Value.of(i)))).thenReturn(values.get(i));
        }
    }

    // optional method to help debugging clock cycle issues
    private void printClockCycles()
    {
        doAnswer(_ -> {
            // TODO locate the location in the stack trace that called Clock::nextCycle
            StackTraceElement currentExecution = new Exception().getStackTrace()[8];
            System.out.printf("Clock::nextCycle %s.%s(%s:%d)%n",
                              currentExecution.getClassName(),
                              currentExecution.getMethodName(),
                              currentExecution.getFileName(),
                              currentExecution.getLineNumber());
            return null;
        }).when(clock).nextCycle();
    }

    @Test
    void execute_ADC_Absolute()
    {
        // given
        accumulator.store(Value.of(0x01));

        Flags flags = cpu.getFlags().toBuilder().carry(false).build();
        cpu.setFlags(flags);

        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        setNextOp(adc(absolute(target)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.of(0x11)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
                                                                  .negative(false)
                                                                  .overflow(false)
                                                                  .zero(false)
                                                                  .carry(false)
                                                                  .build()));
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

        Flags flags = cpu.getFlags().toBuilder().carry(carry != 0).build();
        cpu.setFlags(flags);

        setNextOp(adc(immediate(Value.ofHex(input))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
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

        Flags flags = cpu.getFlags();

        Address target = Address.of(0x1234);
        Value value = Value.of(0b0101);
        when(reader.read(target)).thenReturn(value);

        setNextOp(and(absolute(target)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.of(0b0100)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder().negative(false).zero(false).build()));
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

        Flags flags = cpu.getFlags();

        setNextOp(and(immediate(Value.ofHex(input))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
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

        Flags flags = cpu.getFlags();

        setNextOp(asl(accumulator()));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofBits(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .carry(isCarry)
                                                                  .build()));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 1, 0003, 2",
                "0001, 10, 0, 0013, 3",
                "00FD, 02, 0, 0101, 4",
                "0000, FD, 0, 00FF, 3",
                "FFFD, 01, 0, 0000, 4"})
    void execute_BCC_Relative(String start, String displacement, int carry, String expectedPC, int expectedCycles)
    {
        // given
        cpu.execute(jmp(absolute(Address.ofHex(start))));

        Flags flags = cpu.getFlags().toBuilder().carry(carry != 0).build();
        cpu.setFlags(flags);

        reset(clock);
        setNextOp(bcc(relative(Value.ofHex(displacement))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0, 0003, 2",
                "0001, 10, 1, 0013, 3",
                "00FD, 02, 1, 0101, 4",
                "0000, FD, 1, 00FF, 3",
                "FFFD, 01, 1, 0000, 4"})
    void execute_BCS_Relative(String start, String displacement, int carry, String expectedPC, int expectedCycles)
    {
        // given
        cpu.execute(jmp(absolute(Address.ofHex(start))));

        Flags flags = cpu.getFlags().toBuilder().carry(carry != 0).build();
        cpu.setFlags(flags);

        reset(clock);
        setNextOp(bcs(relative(Value.ofHex(displacement))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0, 0003, 2",
                "0001, 10, 1, 0013, 3",
                "00FD, 02, 1, 0101, 4",
                "0000, FD, 1, 00FF, 3",
                "FFFD, 01, 1, 0000, 4"})
    void execute_BEQ_Relative(String start, String displacement, int zero, String expectedPC, int expectedCycles)
    {
        // given
        cpu.execute(jmp(absolute(Address.ofHex(start))));

        Flags flags = cpu.getFlags().toBuilder().zero(zero != 0).build();
        cpu.setFlags(flags);

        reset(clock);
        setNextOp(beq(relative(Value.ofHex(displacement))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, false, true",
                "00, FF, true, true, true",
                "FF, FF, true, true, false",
                "FF, 00, false, false, true",
                "04, 87, true, false, false"})
    void execute_BIT_Absolute(String acc, String input, boolean bit7, boolean bit6, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        Flags flags = cpu.getFlags();

        Address target = Address.of(0x1234);
        Value value = Value.ofHex(input);
        when(reader.read(target)).thenReturn(value);

        setNextOp(bit(absolute(target)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder().negative(bit7).overflow(bit6).zero(isZero).build());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, true", "00, FF, true", "FF, FF, false", "FF, 00, true", "04, 87, false"})
    void execute_BIT_Immediate(String acc, String input, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        Flags flags = cpu.getFlags();

        setNextOp(bit(immediate(Value.ofHex(input))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder().zero(isZero).build());
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0, 0003, 2",
                "0001, 10, 1, 0013, 3",
                "00FD, 02, 1, 0101, 4",
                "0000, FD, 1, 00FF, 3",
                "FFFD, 01, 1, 0000, 4"})
    void execute_BMI_Relative(String start, String displacement, int negative, String expectedPC, int expectedCycles)
    {
        // given
        cpu.execute(jmp(absolute(Address.ofHex(start))));

        Flags flags = cpu.getFlags().toBuilder().negative(negative != 0).build();
        cpu.setFlags(flags);

        reset(clock);
        setNextOp(bmi(relative(Value.ofHex(displacement))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 1, 0003, 2",
                "0001, 10, 0, 0013, 3",
                "00FD, 02, 0, 0101, 4",
                "0000, FD, 0, 00FF, 3",
                "FFFD, 01, 0, 0000, 4"})
    void execute_BNE_Relative(String start, String displacement, int zero, String expectedPC, int expectedCycles)
    {
        // given
        cpu.execute(jmp(absolute(Address.ofHex(start))));

        Flags flags = cpu.getFlags().toBuilder().zero(zero != 0).build();
        cpu.setFlags(flags);

        reset(clock);
        setNextOp(bne(relative(Value.ofHex(displacement))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 1, 0003, 2",
                "0001, 10, 0, 0013, 3",
                "00FD, 02, 0, 0101, 4",
                "0000, FD, 0, 00FF, 3",
                "FFFD, 01, 0, 0000, 4"})
    void execute_BPL_Relative(String start, String displacement, int negative, String expectedPC, int expectedCycles)
    {
        // given
        cpu.execute(jmp(absolute(Address.ofHex(start))));

        Flags flags = cpu.getFlags().toBuilder().negative(negative != 0).build();
        cpu.setFlags(flags);

        reset(clock);
        setNextOp(bpl(relative(Value.ofHex(displacement))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0013, 3", "00FD, 02, 0101, 4", "0000, FD, 00FF, 3", "FFFD, 01, 0000, 4"})
    void execute_BRA_Relative(String start, String displacement, String expectedPC, int expectedCycles)
    {
        // given
        cpu.execute(jmp(absolute(Address.ofHex(start))));

        Flags flags = cpu.getFlags();

        reset(clock);
        setNextOp(bra(relative(Value.ofHex(displacement))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @CsvSource({"00, FF, true, false",
                "01, 00, false, true",
                "FF, FE, true, false"})
    void execute_DEC_Accumulator(String acc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        Flags flags = cpu.getFlags();

        setNextOp(dec(accumulator()));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
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

        Flags flags = cpu.getFlags();

        setNextOp(inc(accumulator()));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @Test
    void execute_JMP_Absolute()
    {
        // given
        Flags flags = cpu.getFlags();

        Address address = Address.of(0x1234);
        setNextOp(jmp(absolute(address)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(address),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @Test
    void execute_JSR_Absolute()
    {
        // given
        var start = Address.of(0x1234);
        cpu.execute(jmp(absolute(start)));

        Flags flags = cpu.getFlags();

        reset(clock);
        var target = Address.of(0x3000);
        setNextOp(jsr(absolute(target)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(target),
                  () -> assertThat(Address.of(stack.pop(), stack.pop())).isEqualTo(start.plus(Value.of(2))),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @Test
    void execute_LDA_Absolute()
    {
        // given
        accumulator.store(Value.ZERO);

        Flags flags = cpu.getFlags();

        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        setNextOp(lda(absolute(target)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(value),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder().negative(false).zero(false).build()));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true",
                "01, 01, false, false",
                "FF, FF, true, false"})
    void execute_LDA_Immediate(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ZERO);

        Flags flags = cpu.getFlags();

        setNextOp(lda(immediate(Value.ofHex(input))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @Test
    void execute_NOP_Immediate()
    {
        // given
        Flags flags = cpu.getFlags();

        setNextOp(nop());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertThat(cpu.getFlags()).isEqualTo(flags);
    }

    @Test
    void execute_ORA_Absolute()
    {
        // given
        accumulator.store(Value.of(0b1100));

        Flags flags = cpu.getFlags();

        Address target = Address.of(0x1234);
        Value value = Value.of(0b0101);
        when(reader.read(target)).thenReturn(value);

        setNextOp(ora(absolute(target)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.of(0b1101)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder().negative(false).zero(false).build()));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00, false, true",
                "00, FF, FF, true, false",
                "FF, FF, FF, true, false",
                "FF, 00, FF, true, false",
                "04, 07, 07, false, false"})
    void execute_ORA_Immediate(String acc, String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        Flags flags = cpu.getFlags();

        setNextOp(ora(immediate(Value.ofHex(input))));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
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

        Flags flags = cpu.getFlags();

        setNextOp(pha());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
        assertAll(() -> assertThat(stack.pop()).isEqualTo(Value.ofHex(acc)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true",
                "01, 01, false, false",
                "FF, FF, true, false"})
    void execute_PLA_Stack(String initStack, String expected, boolean isNegative, boolean isZero)
    {
        // given
        stack.push(Value.ofHex(initStack));
        reset(clock);

        Flags flags = cpu.getFlags();

        setNextOp(pla());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags.toBuilder()
                                                                  .negative(isNegative)
                                                                  .zero(isZero)
                                                                  .build()));
    }

    @Test
    void execute_RTS_Stack()
    {
        // given
        stack.push(Value.of(0x12));
        stack.push(Value.of(0x33));

        Flags flags = cpu.getFlags();

        reset(clock);
        setNextOp(rts());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.of(0x1234)),
                  () -> assertThat(stack.isEmpty()).isTrue(),
                  () -> assertThat(cpu.getFlags()).isEqualTo(flags));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_STA_Absolute(String acc)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        Flags flags = cpu.getFlags();

        var target = Address.of(0x1234);
        setNextOp(sta(absolute(target)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        verify(writer).write(target, Value.ofHex(acc));
        assertThat(cpu.getFlags()).isEqualTo(flags);
    }
}
