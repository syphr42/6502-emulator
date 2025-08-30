package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.syphr.cpu6502.emulator.machine.AddressMode.*;
import static org.syphr.cpu6502.emulator.machine.Operation.*;

@ExtendWith(MockitoExtension.class)
class CPUTest
{
    @Mock
    Clock clock;

    @Mock
    Reader reader;

    @Mock
    Writer writer;

    @InjectMocks
    CPU cpu;

    Register accumulator;
    Register x;
    Register y;
    StatusRegister status;
    Stack stack;
    ProgramManager programManager;

    @BeforeEach
    void beforeEach()
    {
        accumulator = cpu.getAccumulator();
        x = cpu.getX();
        y = cpu.getY();
        status = cpu.getStatus();
        stack = cpu.getStack();
        programManager = cpu.getProgramManager();

        // set pc clear of the zero page and stack for testing
        programManager.setProgramCounter(Address.of(0x8000));
    }

    private void setNextOp(Operation op)
    {
        Address pc = programManager.getProgramCounter();

        List<Value> values = toValues(op);
        for (int i = 0; i < values.size(); i++) {
            when(reader.read(pc.plus(Value.of(i)))).thenReturn(values.get(i));
        }

        // single byte operations always perform a second dummy read
        if (values.size() == 1) {
            when(reader.read(pc.increment())).thenReturn(Value.ZERO);
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
        status.setCarry(false);

        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        setNextOp(adc(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(Value.of(0x11),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(false).overflow(false).zero(false).carry(false).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
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
        status.setCarry(carry != 0);

        setNextOp(adc(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags()
                         .toBuilder()
                         .negative(isNegative)
                         .overflow(isOverflow)
                         .zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @Test
    void execute_AND_Absolute()
    {
        // given
        accumulator.store(Value.of(0b1100));

        Address target = Address.of(0x1234);
        Value value = Value.of(0b0101);
        when(reader.read(target)).thenReturn(value);

        setNextOp(and(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(Value.of(0b0100),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(false).zero(false).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
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

        setNextOp(and(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00000000, 00000000, false, true, false",
                "00000001, 00000010, false, false, false",
                "10000000, 00000000, false, true, true",
                "10101010, 01010100, false, false, true",
                "01010101, 10101010, true, false, false",
                "11000000, 10000000, true, false, true"})
    void execute_ASL_Absolute(String memory, String expected, boolean isNegative, boolean isZero, boolean isCarry)
    {
        // given
        var address = Address.of(0x1234);
        when(reader.read(address)).thenReturn(Value.ofBits(memory));

        setNextOp(asl(absolute(address)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        verify(writer).write(address, Value.ofBits(expected));
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
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

        setNextOp(asl(accumulator()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofBits(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
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
        programManager.setProgramCounter(Address.ofHex(start));
        status.setCarry(carry != 0);

        setNextOp(bcc(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
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
        programManager.setProgramCounter(Address.ofHex(start));
        status.setCarry(carry != 0);

        setNextOp(bcs(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
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
        programManager.setProgramCounter(Address.ofHex(start));
        status.setZero(zero != 0);

        setNextOp(beq(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
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

        Address target = Address.of(0x1234);
        Value value = Value.ofHex(input);
        when(reader.read(target)).thenReturn(value);

        setNextOp(bit(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(Value.ofHex(acc),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(bit7).overflow(bit6).zero(isZero).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, true", "00, FF, true", "FF, FF, false", "FF, 00, true", "04, 87, false"})
    void execute_BIT_Immediate(String acc, String input, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(bit(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(acc),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().zero(isZero).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
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
        programManager.setProgramCounter(Address.ofHex(start));
        status.setNegative(negative != 0);

        setNextOp(bmi(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
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
        programManager.setProgramCounter(Address.ofHex(start));
        status.setZero(zero != 0);

        setNextOp(bne(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
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
        programManager.setProgramCounter(Address.ofHex(start));
        status.setNegative(negative != 0);

        setNextOp(bpl(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0013, 3", "00FD, 02, 0101, 4", "0000, FD, 00FF, 3", "FFFD, 01, 0000, 4"})
    void execute_BRA_Relative(String start, String displacement, String expectedPC, int expectedCycles)
    {
        // given
        programManager.setProgramCounter(Address.ofHex(start));

        setNextOp(bra(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 1, 0003, 2",
                "0001, 10, 0, 0013, 3",
                "00FD, 02, 0, 0101, 4",
                "0000, FD, 0, 00FF, 3",
                "FFFD, 01, 0, 0000, 4"})
    void execute_BVC_Relative(String start, String displacement, int overflow, String expectedPC, int expectedCycles)
    {
        // given
        programManager.setProgramCounter(Address.ofHex(start));
        status.setOverflow(overflow != 0);

        setNextOp(bvc(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0, 0003, 2",
                "0001, 10, 1, 0013, 3",
                "00FD, 02, 1, 0101, 4",
                "0000, FD, 1, 00FF, 3",
                "FFFD, 01, 1, 0000, 4"})
    void execute_BVS_Relative(String start, String displacement, int overflow, String expectedPC, int expectedCycles)
    {
        // given
        programManager.setProgramCounter(Address.ofHex(start));
        status.setOverflow(overflow != 0);

        setNextOp(bvs(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(expectedCycles)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.ofHex(expectedPC),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void execute_CLC_Implied(boolean carry)
    {
        // given
        status.setCarry(carry);

        setNextOp(clc());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().carry(false).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void execute_CLD_Implied(boolean decimal)
    {
        // given
        status.setDecimal(decimal);

        setNextOp(cld());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().decimal(false).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void execute_CLI_Implied(boolean irqDisable)
    {
        // given
        status.setIrqDisable(irqDisable);

        setNextOp(cli());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().irqDisable(false).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void execute_CLV_Implied(boolean overflow)
    {
        // given
        status.setOverflow(overflow);

        setNextOp(clv());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().overflow(false).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true, true",
                "00, FF, false, false, false",
                "FF, FF, false, true, true",
                "FF, 00, true, false, true",
                "04, 02, false, false, true"})
    void execute_CMP_Absolute(String acc, String input, boolean isNegative, boolean isZero, boolean isCarry)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        Address target = Address.of(0x1234);
        Value value = Value.ofHex(input);
        when(reader.read(target)).thenReturn(value);

        setNextOp(cmp(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(Value.ofHex(acc),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true, true",
                "00, FF, false, false, false",
                "FF, FF, false, true, true",
                "FF, 00, true, false, true",
                "04, 02, false, false, true"})
    void execute_CMP_Immediate(String acc, String input, boolean isNegative, boolean isZero, boolean isCarry)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(cmp(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(acc),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true, true",
                "00, FF, false, false, false",
                "FF, FF, false, true, true",
                "FF, 00, true, false, true",
                "04, 02, false, false, true"})
    void execute_CPX_Absolute(String xVal, String input, boolean isNegative, boolean isZero, boolean isCarry)
    {
        // given
        x.store(Value.ofHex(xVal));

        Address target = Address.of(0x1234);
        Value value = Value.ofHex(input);
        when(reader.read(target)).thenReturn(value);

        setNextOp(cpx(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(state.accumulator(),
                    Value.ofHex(xVal),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true, true",
                "00, FF, false, false, false",
                "FF, FF, false, true, true",
                "FF, 00, true, false, true",
                "04, 02, false, false, true"})
    void execute_CPX_Immediate(String xVal, String input, boolean isNegative, boolean isZero, boolean isCarry)
    {
        // given
        x.store(Value.ofHex(xVal));

        setNextOp(cpx(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    Value.ofHex(xVal),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true, true",
                "00, FF, false, false, false",
                "FF, FF, false, true, true",
                "FF, 00, true, false, true",
                "04, 02, false, false, true"})
    void execute_CPY_Absolute(String yVal, String input, boolean isNegative, boolean isZero, boolean isCarry)
    {
        // given
        y.store(Value.ofHex(yVal));

        Address target = Address.of(0x1234);
        Value value = Value.ofHex(input);
        when(reader.read(target)).thenReturn(value);

        setNextOp(cpy(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    Value.ofHex(yVal),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true, true",
                "00, FF, false, false, false",
                "FF, FF, false, true, true",
                "FF, 00, true, false, true",
                "04, 02, false, false, true"})
    void execute_CPY_Immediate(String yVal, String input, boolean isNegative, boolean isZero, boolean isCarry)
    {
        // given
        y.store(Value.ofHex(yVal));

        setNextOp(cpy(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    Value.ofHex(yVal),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, FF, true, false", "01, 00, false, true", "FF, FE, true, false"})
    void execute_DEC_Accumulator(String acc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(dec(accumulator()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, FF, true, false", "01, 00, false, true", "FF, FE, true, false"})
    void execute_DEX_Implied(String xVal, String expected, boolean isNegative, boolean isZero)
    {
        // given
        x.store(Value.ofHex(xVal));

        setNextOp(dex());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    Value.ofHex(expected),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, FF, true, false", "01, 00, false, true", "FF, FE, true, false"})
    void execute_DEY_Implied(String yVal, String expected, boolean isNegative, boolean isZero)
    {
        // given
        y.store(Value.ofHex(yVal));

        setNextOp(dey());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    Value.ofHex(expected),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00, false, true",
                "00, FF, FF, true, false",
                "FF, FF, 00, false, true",
                "FF, 00, FF, true, false",
                "04, 07, 03, false, false"})
    void execute_EOR_Absolute(String acc, String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        Address target = Address.of(0x1234);
        Value value = Value.ofHex(input);
        when(reader.read(target)).thenReturn(value);

        setNextOp(eor(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, 00, false, true",
                "00, FF, FF, true, false",
                "FF, FF, 00, false, true",
                "FF, 00, FF, true, false",
                "04, 07, 03, false, false"})
    void execute_EOR_Immediate(String acc, String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(eor(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"FF, 00, false, true", "00, 01, false, false", "FE, FF, true, false"})
    void execute_INC_Accumulator(String acc, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(inc(accumulator()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"FF, 00, false, true", "00, 01, false, false", "FE, FF, true, false"})
    void execute_INX_Implied(String xVal, String expected, boolean isNegative, boolean isZero)
    {
        // given
        x.store(Value.ofHex(xVal));

        setNextOp(inx());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    Value.ofHex(expected),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"FF, 00, false, true", "00, 01, false, false", "FE, FF, true, false"})
    void execute_INY_Implied(String yVal, String expected, boolean isNegative, boolean isZero)
    {
        // given
        y.store(Value.ofHex(yVal));

        setNextOp(iny());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    Value.ofHex(expected),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @Test
    void execute_JMP_Absolute()
    {
        // given
        Address address = Address.of(0x1234);
        setNextOp(jmp(absolute(address)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    address,
                    state.stackPointer(),
                    state.stackData());
    }

    @Test
    void execute_JSR_Absolute()
    {
        // given
        var start = Address.of(0x1234);
        programManager.setProgramCounter(start);

        var target = Address.of(0x3000);
        setNextOp(jsr(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        verify(writer).write(state.stackPointer(), Value.of(0x12));
        verify(writer).write(decrementLow(state.stackPointer()), Value.of(0x36));
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    target,
                    decrementLow(decrementLow(state.stackPointer())),
                    List.of(Value.of(0x36), Value.of(0x12)));
    }

    @Test
    void execute_LDA_Absolute()
    {
        // given
        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        setNextOp(lda(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(value,
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(false).zero(false).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true", "01, 01, false, false", "FF, FF, true, false"})
    void execute_LDA_Immediate(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        setNextOp(lda(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @Test
    void execute_LDX_Absolute()
    {
        // given
        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        setNextOp(ldx(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(state.accumulator(),
                    value,
                    state.y(),
                    state.flags().toBuilder().negative(false).zero(false).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true", "01, 01, false, false", "FF, FF, true, false"})
    void execute_LDX_Immediate(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        setNextOp(ldx(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    Value.ofHex(expected),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @Test
    void execute_LDY_Absolute()
    {
        // given
        Address target = Address.of(0x1234);
        Value value = Value.of(0x10);
        when(reader.read(target)).thenReturn(value);

        setNextOp(ldy(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    value,
                    state.flags().toBuilder().negative(false).zero(false).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true", "01, 01, false, false", "FF, FF, true, false"})
    void execute_LDY_Immediate(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        setNextOp(ldy(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    Value.ofHex(expected),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00000000, 00000000, true, false",
                "10000000, 01000000, false, false",
                "00000001, 00000000, true, true",
                "01010101, 00101010, false, true",
                "10101010, 01010101, false, false",
                "00000011, 00000001, false, true"})
    void execute_LSR_Absolute(String memory, String expected, boolean isZero, boolean isCarry)
    {
        // given
        var address = Address.of(0x1234);
        when(reader.read(address)).thenReturn(Value.ofBits(memory));

        setNextOp(lsr(absolute(address)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        verify(writer).write(address, Value.ofBits(expected));
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(false).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00000000, 00000000, true, false",
                "10000000, 01000000, false, false",
                "00000001, 00000000, true, true",
                "01010101, 00101010, false, true",
                "10101010, 01010101, false, false",
                "00000011, 00000001, false, true"})
    void execute_LSR_Accumulator(String acc, String expected, boolean isZero, boolean isCarry)
    {
        // given
        accumulator.store(Value.ofBits(acc));

        setNextOp(lsr(accumulator()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofBits(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(false).zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @Test
    void execute_NOP_Immediate()
    {
        // given
        setNextOp(nop());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @Test
    void execute_ORA_Absolute()
    {
        // given
        accumulator.store(Value.of(0b1100));

        Address target = Address.of(0x1234);
        Value value = Value.of(0b0101);
        when(reader.read(target)).thenReturn(value);

        setNextOp(ora(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(Value.of(0b1101),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(false).zero(false).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
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

        setNextOp(ora(immediate(Value.ofHex(input))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(2)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_PHA_Stack(String acc)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        setNextOp(pha());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    state.programCounter().plus(Value.of(1)),
                    decrementLow(state.stackPointer()),
                    List.of(accumulator.value()));
    }

    @ParameterizedTest
    @CsvSource({"true, true, true, true, true, true, true, true",
                "false, true, true, true, true, true, true, true",
                "false, false, true, true, true, true, true, true",
                "false, false, false, true, true, true, true, true",
                "false, false, false, false, true, true, true, true",
                "false, false, false, false, false, true, true, true",
                "false, false, false, false, false, false, true, true",
                "false, false, false, false, false, false, false, true",
                "false, false, false, false, false, false, false, false"})
    void execute_PHP_Stack(boolean isNegative,
                           boolean isOverflow,
                           boolean isUser,
                           boolean isBreakCommand,
                           boolean isDecimal,
                           boolean isIrqDisable,
                           boolean isZero,
                           boolean isCarry)
    {
        // given
        status.setNegative(isNegative)
              .setOverflow(isOverflow)
              .setUser(isUser)
              .setBreakCommand(isBreakCommand)
              .setDecimal(isDecimal)
              .setIrqDisable(isIrqDisable)
              .setZero(isZero)
              .setCarry(isCarry);

        setNextOp(php());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    state.programCounter().plus(Value.of(1)),
                    decrementLow(state.stackPointer()),
                    List.of(status.value()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_PHX_Stack(String xVal)
    {
        // given
        x.store(Value.ofHex(xVal));

        setNextOp(phx());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    state.programCounter().plus(Value.of(1)),
                    decrementLow(state.stackPointer()),
                    List.of(x.value()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_PHY_Stack(String yVal)
    {
        // given
        y.store(Value.ofHex(yVal));

        setNextOp(phy());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(3)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    state.programCounter().plus(Value.of(1)),
                    decrementLow(state.stackPointer()),
                    List.of(y.value()));
    }

    @ParameterizedTest
    @CsvSource({"00, false, true", "01, false, false", "FF, true, false"})
    void execute_PLA_Stack(String input, boolean isNegative, boolean isZero)
    {
        // given
        Value value = Value.ofHex(input);

        when(reader.read(incrementLow(stack.getPointer()))).thenReturn(value);

        reset(clock);
        setNextOp(pla());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(value,
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    incrementLow(state.stackPointer()),
                    List.of());
    }

    @ParameterizedTest
    @CsvSource({"11111111, true, true, true, true, true, true, true, true",
                "01111111, false, true, true, true, true, true, true, true",
                "00111111, false, false, true, true, true, true, true, true",
                "00011111, false, false, false, true, true, true, true, true",
                "00001111, false, false, false, false, true, true, true, true",
                "00000111, false, false, false, false, false, true, true, true",
                "00000011, false, false, false, false, false, false, true, true",
                "00000001, false, false, false, false, false, false, false, true",
                "00000000, false, false, false, false, false, false, false, false"})
    void execute_PLP_Stack(String input,
                           boolean isNegative,
                           boolean isOverflow,
                           boolean isUser,
                           boolean isBreakCommand,
                           boolean isDecimal,
                           boolean isIrqDisable,
                           boolean isZero,
                           boolean isCarry)
    {
        // given
        Value value = Value.ofBits(input);

        when(reader.read(incrementLow(stack.getPointer()))).thenReturn(value);

        reset(clock);
        setNextOp(plp());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    new Flags(isNegative, isOverflow, isUser, isBreakCommand, isDecimal, isIrqDisable, isZero, isCarry),
                    state.programCounter().plus(Value.of(1)),
                    incrementLow(state.stackPointer()),
                    List.of());
    }

    @ParameterizedTest
    @CsvSource({"00, false, true", "01, false, false", "FF, true, false"})
    void execute_PLX_Stack(String input, boolean isNegative, boolean isZero)
    {
        // given
        Value value = Value.ofHex(input);

        when(reader.read(incrementLow(stack.getPointer()))).thenReturn(value);

        reset(clock);
        setNextOp(plx());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(state.accumulator(),
                    value,
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    incrementLow(state.stackPointer()),
                    List.of());
    }

    @ParameterizedTest
    @CsvSource({"00, false, true", "01, false, false", "FF, true, false"})
    void execute_PLY_Stack(String input, boolean isNegative, boolean isZero)
    {
        // given
        Value value = Value.ofHex(input);

        when(reader.read(incrementLow(stack.getPointer()))).thenReturn(value);

        reset(clock);
        setNextOp(ply());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    value,
                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                    state.programCounter().plus(Value.of(1)),
                    incrementLow(state.stackPointer()),
                    List.of());
    }

    @ParameterizedTest
    @CsvSource({"0, 00000000, 00000000, 0, false, true",
                "0, 00000001, 00000000, 1, false, true",
                "1, 00000000, 10000000, 0, true, false",
                "1, 00000001, 10000000, 1, true, false",
                "0, 11111111, 01111111, 1, false, false"})
    void execute_ROR_Absolute(int carry,
                              String memory,
                              String expected,
                              int expectedCarry,
                              boolean isNegative,
                              boolean isZero)
    {
        // given
        status.setCarry(carry != 0);

        var address = Address.of(0x1234);
        when(reader.read(address)).thenReturn(Value.ofBits(memory));

        setNextOp(ror(absolute(address)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        verify(writer).write(address, Value.ofBits(expected));
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(expectedCarry != 0).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"0, 00000000, 00000000, 0, false, true",
                "0, 00000001, 00000000, 1, false, true",
                "1, 00000000, 10000000, 0, true, false",
                "1, 00000001, 10000000, 1, true, false",
                "0, 11111111, 01111111, 1, false, false"})
    void execute_ROR_Accumulator(int carry,
                                 String acc,
                                 String expected,
                                 int expectedCarry,
                                 boolean isNegative,
                                 boolean isZero)
    {
        // given
        accumulator.store(Value.ofBits(acc));
        status.setCarry(carry != 0);

        setNextOp(ror(accumulator()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(Value.ofBits(expected),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().negative(isNegative).zero(isZero).carry(expectedCarry != 0).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @Test
    void execute_RTS_Stack()
    {
        // given
        when(reader.read(incrementLow(stack.getPointer()))).thenReturn(Value.of(0x33));
        when(reader.read(incrementLow(incrementLow(stack.getPointer())))).thenReturn(Value.of(0x12));

        reset(clock);
        setNextOp(rts());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    Address.of(0x1234),
                    incrementLow(incrementLow(state.stackPointer())),
                    List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_STA_Absolute(String acc)
    {
        // given
        accumulator.store(Value.ofHex(acc));

        var target = Address.of(0x1234);
        setNextOp(sta(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        verify(writer).write(target, Value.ofHex(acc));
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    private Address incrementLow(Address address)
    {
        return Address.of(address.low().increment(), address.high());
    }

    private Address decrementLow(Address address)
    {
        return Address.of(address.low().decrement(), address.high());
    }

    private void assertState(Value accumulator,
                             Value x,
                             Value y,
                             Flags flags,
                             Address programCounter,
                             Address stackPointer,
                             List<Value> stackData)
    {
        assertThat(cpu.getState()).extracting(CPUState::accumulator,
                                              CPUState::x,
                                              CPUState::y,
                                              CPUState::flags,
                                              CPUState::programCounter,
                                              CPUState::stackPointer,
                                              CPUState::stackData)
                                  .containsExactly(accumulator, x, y, flags, programCounter, stackPointer, stackData);
    }
}
