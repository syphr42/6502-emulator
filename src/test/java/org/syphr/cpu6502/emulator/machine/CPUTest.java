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
        List<Value> values = Operation.toValues(op);
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

        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        setNextOp(Operation.adc(target));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
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
        cpu.setFlags(cpu.getFlags().toBuilder().carry(carry != 0).build());

        setNextOp(Operation.adc(Value.ofHex(input)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).extracting(Flags::negative,
                                                              Flags::overflow,
                                                              Flags::zero,
                                                              Flags::carry)
                                                  .containsExactly(isNegative, isOverflow, isZero, isCarry));
    }

    @Test
    void execute_AND_Absolute()
    {
        // given
        accumulator.store(Value.of(0b1100));

        Address target = Address.of(0x1234);
        Value value = Value.of(0b0101);
        when(reader.read(target)).thenReturn(value);

        setNextOp(Operation.and(target));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
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

        setNextOp(Operation.and(Value.ofHex(input)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).extracting(Flags::negative, Flags::zero)
                                                  .containsExactly(isNegative, isZero));
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

        setNextOp(Operation.asl());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofBits(expected)),
                  () -> assertThat(cpu.getFlags()).extracting(Flags::negative,
                                                              Flags::zero,
                                                              Flags::carry)
                                                  .containsExactly(isNegative, isZero, isCarry));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 1, 0003, 2",
                "0001, 10, 0, 0013, 3",
                "00FD, 02, 0, 0101, 4",
                "0000, FD, 0, 00FF, 3",
                "FFFD, 01, 0, 0000, 4"})
    void execute_BCC_ProgramCounterRelative(String start,
                                            String displacement,
                                            int carry,
                                            String expectedPC,
                                            int expectedCycles)
    {
        // given
        cpu.execute(Operation.jmp(Address.ofHex(start)));
        cpu.setFlags(cpu.getFlags().toBuilder().carry(carry != 0).build());
        reset(clock);

        setNextOp(Operation.bcc(Value.ofHex(displacement)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0, 0003, 2",
                "0001, 10, 1, 0013, 3",
                "00FD, 02, 1, 0101, 4",
                "0000, FD, 1, 00FF, 3",
                "FFFD, 01, 1, 0000, 4"})
    void execute_BCS_ProgramCounterRelative(String start,
                                            String displacement,
                                            int carry,
                                            String expectedPC,
                                            int expectedCycles)
    {
        // given
        cpu.execute(Operation.jmp(Address.ofHex(start)));
        cpu.setFlags(cpu.getFlags().toBuilder().carry(carry != 0).build());
        reset(clock);

        setNextOp(Operation.bcs(Value.ofHex(displacement)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0, 0003, 2",
                "0001, 10, 1, 0013, 3",
                "00FD, 02, 1, 0101, 4",
                "0000, FD, 1, 00FF, 3",
                "FFFD, 01, 1, 0000, 4"})
    void execute_BEQ_ProgramCounterRelative(String start,
                                            String displacement,
                                            int zero,
                                            String expectedPC,
                                            int expectedCycles)
    {
        // given
        cpu.execute(Operation.jmp(Address.ofHex(start)));
        cpu.setFlags(cpu.getFlags().toBuilder().zero(zero != 0).build());
        reset(clock);

        setNextOp(Operation.beq(Value.ofHex(displacement)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertThat(cpu.getProgramCounter()).isEqualTo(Address.ofHex(expectedPC));
    }

    @ParameterizedTest
    @CsvSource({"00, FF, true, false",
                "01, 00, false, true",
                "FF, FE, true, false"})
    void execute_DEC_Accumulator(String acc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(Operation.dec());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).extracting(Flags::negative, Flags::zero)
                                                  .containsExactly(isNegative, isZero));
    }

    @ParameterizedTest
    @CsvSource({"FF, 00, false, true",
                "00, 01, false, false",
                "FE, FF, true, false"})
    void execute_INC_Accumulator(String acc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(Operation.inc());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).extracting(Flags::negative, Flags::zero)
                                                  .containsExactly(isNegative, isZero));
    }

    @Test
    void execute_JMP_Absolute()
    {
        // given
        Address address = Address.of(0x1234);
        setNextOp(Operation.jmp(address));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
        assertThat(cpu.getProgramCounter()).isEqualTo(address);
    }

    @Test
    void execute_JSR_Absolute()
    {
        // given
        var start = Address.of(0x1234);
        cpu.execute(Operation.jmp(start));
        reset(clock);

        var target = Address.of(0x3000);
        setNextOp(Operation.jsr(target));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(target),
                  () -> assertThat(Address.of(stack.pop(), stack.pop())).isEqualTo(start.plus(Value.of(2))));
    }

    @Test
    void execute_LDA_Absolute()
    {
        // given
        accumulator.store(Value.ZERO);

        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        setNextOp(Operation.lda(target));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
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

        setNextOp(Operation.lda(Value.ofHex(input)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).extracting(Flags::negative, Flags::zero)
                                                  .containsExactly(isNegative, isZero));
    }

    @Test
    void execute_NOP_Immediate()
    {
        // given
        setNextOp(Operation.nop());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
    }

    @Test
    void execute_ORA_Absolute()
    {
        // given
        accumulator.store(Value.of(0b1100));

        Address target = Address.of(0x1234);
        Value value = Value.of(0b0101);
        when(reader.read(target)).thenReturn(value);

        setNextOp(Operation.ora(target));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertThat(accumulator.value()).isEqualTo(Value.of(0b1101));
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

        setNextOp(Operation.ora(Value.ofHex(input)));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).extracting(Flags::negative, Flags::zero)
                                                  .containsExactly(isNegative, isZero));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_PHA_Stack(String acc)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(Operation.pha());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
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
        reset(clock);

        setNextOp(Operation.pla());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertAll(() -> assertThat(accumulator.value()).isEqualTo(Value.ofHex(expected)),
                  () -> assertThat(cpu.getFlags()).extracting(Flags::negative, Flags::zero)
                                                  .containsExactly(isNegative, isZero));
    }

    @Test
    void execute_RTS_Stack()
    {
        // given
        stack.push(Value.of(0x12));
        stack.push(Value.of(0x33));
        reset(clock);

        setNextOp(Operation.rts());

        // when
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        assertAll(() -> assertThat(cpu.getProgramCounter()).isEqualTo(Address.of(0x1234)),
                  () -> assertThat(stack.isEmpty()).isTrue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_STA_Absolute(String acc)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        var target = Address.of(0x1234);
        setNextOp(Operation.sta(target));

        // when
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        verify(writer).write(target, Value.ofHex(acc));
    }
}
