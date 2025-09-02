package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
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

    static Stream<Arguments> execute_ADC()
    {
        return Stream.of(adcInputs(modeAbsolute(), 3, 4),
                         adcInputs(modeAbsoluteXSamePage(), 3, 4),
                         adcInputs(modeAbsoluteXCrossPage(), 3, 5),
                         adcInputs(modeAbsoluteYSamePage(), 3, 4),
                         adcInputs(modeAbsoluteYCrossPage(), 3, 5),
                         adcInputs(modeImmediate(), 2, 2),
                         adcInputs(modeZeroPage(), 2, 3),
                         adcInputs(modeZeroPageXIndirect(), 2, 6),
                         adcInputs(modeZeroPageX(), 2, 4),
                         adcInputs(modeZeroPageIndirect(), 2, 5),
                         adcInputs(modeZeroPageIndirectYSamePage(), 2, 5),
                         adcInputs(modeZeroPageIndirectYCrossPage(), 2, 6))
                     .flatMap(i -> i);
    }

    static Stream<Arguments> adcInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x01, 0, 0x01, mode, cycles, 0x02, false, false, false, false, length),
                         Arguments.of(0xF0, 0, 0x01, mode, cycles, 0xF1, true, false, false, false, length),
                         Arguments.of(0x01, 0, 0xFF, mode, cycles, 0x00, false, false, true, true, length),
                         Arguments.of(0x02, 0, 0xFF, mode, cycles, 0x01, false, false, false, true, length),
                         Arguments.of(0x7F, 0, 0x01, mode, cycles, 0x80, true, true, false, false, length),
                         Arguments.of(0xFF, 0, 0xFF, mode, cycles, 0xFE, true, false, false, true, length),
                         Arguments.of(0x80, 0, 0xFF, mode, cycles, 0x7F, false, true, false, true, length),
                         Arguments.of(0x3F, 1, 0x40, mode, cycles, 0x80, true, true, false, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_ADC(int givenAccumulator,
                     int givenCarry,
                     int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedAccumulator,
                     boolean expectedNegative,
                     boolean expectedOverflow,
                     boolean expectedZero,
                     boolean expectedCarry,
                     int expectedProgramCounterOffset)
    {
        // given
        accumulator.store(Value.of(givenAccumulator));
        status.setCarry(givenCarry != 0);

        setNextOp(adc(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(Value.of(expectedAccumulator),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .overflow(expectedOverflow)
                                         .zero(expectedZero)
                                         .carry(expectedCarry)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_AND()
    {
        return Stream.of(andInputs(modeAbsolute(), 3, 4),
                         andInputs(modeAbsoluteXSamePage(), 3, 4),
                         andInputs(modeAbsoluteXCrossPage(), 3, 5),
                         andInputs(modeAbsoluteYSamePage(), 3, 4),
                         andInputs(modeAbsoluteYCrossPage(), 3, 5),
                         andInputs(modeImmediate(), 2, 2),
                         andInputs(modeZeroPage(), 2, 3),
                         andInputs(modeZeroPageXIndirect(), 2, 6),
                         andInputs(modeZeroPageX(), 2, 4),
                         andInputs(modeZeroPageIndirect(), 2, 5),
                         andInputs(modeZeroPageIndirectYSamePage(), 2, 5),
                         andInputs(modeZeroPageIndirectYCrossPage(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> andInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, 0x00, mode, cycles, 0x00, false, true, length),
                         Arguments.of(0x00, 0xFF, mode, cycles, 0x00, false, true, length),
                         Arguments.of(0xFF, 0xFF, mode, cycles, 0xFF, true, false, length),
                         Arguments.of(0xFF, 0x00, mode, cycles, 0x00, false, true, length),
                         Arguments.of(0b1100, 0b0101, mode, cycles, 0b0100, false, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_AND(int givenAccumulator,
                     int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedAccumulator,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        accumulator.store(Value.of(givenAccumulator));

        setNextOp(and(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(Value.of(expectedAccumulator),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_ASL()
    {
        return Stream.of(aslInputs(modeAbsolute(), 3, 6),
                         aslInputs(modeAbsoluteXSamePage(), 3, 6),
                         aslInputs(modeAbsoluteXCrossPage(), 3, 7),
                         aslInputs(modeAccumulator(), 1, 2),
                         aslInputs(modeZeroPage(), 2, 5),
                         aslInputs(modeZeroPageX(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> aslInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0b00000000, mode, cycles, 0b00000000, false, true, false, length),
                         Arguments.of(0b00000001, mode, cycles, 0b00000010, false, false, false, length),
                         Arguments.of(0b10000000, mode, cycles, 0b00000000, false, true, true, length),
                         Arguments.of(0b10101010, mode, cycles, 0b01010100, false, false, true, length),
                         Arguments.of(0b01010101, mode, cycles, 0b10101010, true, false, false, length),
                         Arguments.of(0b11000000, mode, cycles, 0b10000000, true, false, true, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_ASL(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedOutput,
                     boolean expectedNegative,
                     boolean expectedZero,
                     boolean expectedCarry,
                     int expectedProgramCounterOffset)
    {
        // given
        ModeOutput modeOutput = modeGen.apply(modeInput(input));
        setNextOp(asl(modeOutput.mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> modeOutput.target().ifPresent(target -> verify(writer).write(target, Value.of(expectedOutput))),
                  () -> assertState(modeOutput.mode() instanceof Accumulator
                                    ? Value.of(expectedOutput)
                                    : state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .carry(expectedCarry)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 1, 0003, 2",
                "0001, 10, 0, 0013, 3",
                "00FD, 02, 0, 0101, 4",
                "0000, FD, 0, FFFF, 4",
                "FFFD, 01, 0, 0000, 4",
                "FFF6, FD, 0, FFF5, 3"})
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
                "0000, FD, 1, FFFF, 4",
                "FFFD, 01, 1, 0000, 4",
                "FFF6, FD, 1, FFF5, 3"})
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
                "0000, FD, 1, FFFF, 4",
                "FFFD, 01, 1, 0000, 4",
                "FFF6, FD, 1, FFF5, 3"})
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

    static Stream<Arguments> execute_BIT()
    {
        return Stream.of(bitInputs(modeAbsolute(), 3, 4),
                         bitInputs(modeAbsoluteXSamePage(), 3, 4),
                         bitInputs(modeAbsoluteXCrossPage(), 3, 5),
                         bitInputs(modeImmediate(), 2, 2),
                         bitInputs(modeZeroPage(), 2, 3),
                         bitInputs(modeZeroPageX(), 2, 4)).flatMap(i -> i);
    }

    static Stream<Arguments> bitInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, 0x00, mode, cycles, false, false, true, length),
                         Arguments.of(0x00, 0xFF, mode, cycles, true, true, true, length),
                         Arguments.of(0xFF, 0xFF, mode, cycles, true, true, false, length),
                         Arguments.of(0xFF, 0x00, mode, cycles, false, false, true, length),
                         Arguments.of(0x04, 0x87, mode, cycles, true, false, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_BIT(int givenAccumulator,
                     int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean inputBit7,
                     boolean inputBit6,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        accumulator.store(Value.of(givenAccumulator));

        AddressMode mode = modeGen.apply(modeInput(input)).mode();
        setNextOp(bit(mode));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(), () -> {
            var expectedFlags = state.flags().toBuilder().zero(expectedZero);
            if (!(mode instanceof Immediate)) {
                expectedFlags = expectedFlags.negative(inputBit7).overflow(inputBit6);
            }
            assertState(state.accumulator(),
                        state.x(),
                        state.y(),
                        expectedFlags.build(),
                        state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                        state.stackPointer(),
                        state.stackData());
        });
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 0, 0003, 2",
                "0001, 10, 1, 0013, 3",
                "00FD, 02, 1, 0101, 4",
                "0000, FD, 1, FFFF, 4",
                "FFFD, 01, 1, 0000, 4",
                "FFF6, FD, 1, FFF5, 3"})
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
                "0000, FD, 0, FFFF, 4",
                "FFFD, 01, 0, 0000, 4",
                "FFF6, FD, 0, FFF5, 3"})
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
                "0000, FD, 0, FFFF, 4",
                "FFFD, 01, 0, 0000, 4",
                "FFF6, FD, 0, FFF5, 3"})
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
    @CsvSource({"0001, 10, 0013, 3",
                "00FD, 02, 0101, 4",
                "0000, FD, FFFF, 4",
                "FFFD, 01, 0000, 4",
                "FFF6, FD, FFF5, 3"})
    void execute_BRA_Relative(String start, String displacement, String expectedPC, int expectedCycles)
    {
        // given
        programManager.setProgramCounter(Address.ofHex(start));

        setNextOp(bra(relative(Value.ofHex(displacement))));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    Address.ofHex(expectedPC),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    @ParameterizedTest
    @CsvSource({"0001, 10, 1, 0003, 2",
                "0001, 10, 0, 0013, 3",
                "00FD, 02, 0, 0101, 4",
                "0000, FD, 0, FFFF, 4",
                "FFFD, 01, 0, 0000, 4",
                "FFF6, FD, 0, FFF5, 3"})
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
                "0000, FD, 1, FFFF, 4",
                "FFFD, 01, 1, 0000, 4",
                "FFF6, FD, 1, FFF5, 3"})
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

    static Stream<Arguments> execute_CMP()
    {
        return Stream.of(cmpInputs(modeAbsolute(), 3, 4),
                         cmpInputs(modeAbsoluteXSamePage(), 3, 4),
                         cmpInputs(modeAbsoluteXCrossPage(), 3, 5),
                         cmpInputs(modeAbsoluteYSamePage(), 3, 4),
                         cmpInputs(modeAbsoluteYCrossPage(), 3, 5),
                         cmpInputs(modeImmediate(), 2, 2),
                         cmpInputs(modeZeroPage(), 2, 3),
                         cmpInputs(modeZeroPageXIndirect(), 2, 6),
                         cmpInputs(modeZeroPageX(), 2, 4),
                         cmpInputs(modeZeroPageIndirect(), 2, 5),
                         cmpInputs(modeZeroPageIndirectYSamePage(), 2, 5),
                         cmpInputs(modeZeroPageIndirectYCrossPage(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> cmpInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, 0x00, mode, cycles, false, true, true, length),
                         Arguments.of(0x00, 0xFF, mode, cycles, false, false, false, length),
                         Arguments.of(0xFF, 0xFF, mode, cycles, false, true, true, length),
                         Arguments.of(0xFF, 0x00, mode, cycles, true, false, true, length),
                         Arguments.of(0x04, 0x02, mode, cycles, false, false, true, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_CMP(int givenAccumulator,
                     int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean expectedNegative,
                     boolean expectedZero,
                     boolean expectedCarry,
                     int expectedProgramCounterOffset)
    {
        // given
        accumulator.store(Value.of(givenAccumulator));

        setNextOp(cmp(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .carry(expectedCarry)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_CPX()
    {
        return Stream.of(cpxInputs(modeAbsolute(), 3, 4),
                         cpxInputs(modeImmediate(), 2, 2),
                         cpxInputs(modeZeroPage(), 2, 3)).flatMap(i -> i);
    }

    static Stream<Arguments> cpxInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, 0x00, mode, cycles, false, true, true, length),
                         Arguments.of(0x00, 0xFF, mode, cycles, false, false, false, length),
                         Arguments.of(0xFF, 0xFF, mode, cycles, false, true, true, length),
                         Arguments.of(0xFF, 0x00, mode, cycles, true, false, true, length),
                         Arguments.of(0x04, 0x02, mode, cycles, false, false, true, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_CPX(int givenX,
                     int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean expectedNegative,
                     boolean expectedZero,
                     boolean expectedCarry,
                     int expectedProgramCounterOffset)
    {
        // given
        x.store(Value.of(givenX));

        setNextOp(cpx(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .carry(expectedCarry)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_CPY()
    {
        return Stream.of(cpyInputs(modeAbsolute(), 3, 4),
                         cpyInputs(modeImmediate(), 2, 2),
                         cpyInputs(modeZeroPage(), 2, 3)).flatMap(i -> i);
    }

    static Stream<Arguments> cpyInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, 0x00, mode, cycles, false, true, true, length),
                         Arguments.of(0x00, 0xFF, mode, cycles, false, false, false, length),
                         Arguments.of(0xFF, 0xFF, mode, cycles, false, true, true, length),
                         Arguments.of(0xFF, 0x00, mode, cycles, true, false, true, length),
                         Arguments.of(0x04, 0x02, mode, cycles, false, false, true, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_CPY(int givenY,
                     int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean expectedNegative,
                     boolean expectedZero,
                     boolean expectedCarry,
                     int expectedProgramCounterOffset)
    {
        // given
        y.store(Value.of(givenY));

        setNextOp(cpy(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .carry(expectedCarry)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_DEC()
    {
        return Stream.of(decInputs(modeAbsolute(), 3, 6),
                         decInputs(modeAbsoluteXSamePage(), 3, 6),
                         decInputs(modeAbsoluteXCrossPage(), 3, 7),
                         decInputs(modeAccumulator(), 1, 2),
                         decInputs(modeZeroPage(), 2, 5),
                         decInputs(modeZeroPageX(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> decInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, mode, cycles, 0xFF, true, false, length),
                         Arguments.of(0x01, mode, cycles, 0x00, false, true, length),
                         Arguments.of(0x02, mode, cycles, 0x01, false, false, length),
                         Arguments.of(0xFF, mode, cycles, 0xFE, true, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_DEC(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedOutput,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        ModeOutput modeOutput = modeGen.apply(modeInput(input));
        setNextOp(dec(modeOutput.mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> modeOutput.target().ifPresent(target -> verify(writer).write(target, Value.of(expectedOutput))),
                  () -> assertState(modeOutput.mode() instanceof Accumulator
                                    ? Value.of(expectedOutput)
                                    : state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
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

    static Stream<Arguments> execute_EOR()
    {
        return Stream.of(eorInputs(modeAbsolute(), 3, 4),
                         eorInputs(modeAbsoluteXSamePage(), 3, 4),
                         eorInputs(modeAbsoluteXCrossPage(), 3, 5),
                         eorInputs(modeAbsoluteYSamePage(), 3, 4),
                         eorInputs(modeAbsoluteYCrossPage(), 3, 5),
                         eorInputs(modeImmediate(), 2, 2),
                         eorInputs(modeZeroPage(), 2, 3),
                         eorInputs(modeZeroPageXIndirect(), 2, 6),
                         eorInputs(modeZeroPageX(), 2, 4),
                         eorInputs(modeZeroPageIndirect(), 2, 5),
                         eorInputs(modeZeroPageIndirectYSamePage(), 2, 5),
                         eorInputs(modeZeroPageIndirectYCrossPage(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> eorInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, 0x00, mode, cycles, 0x00, false, true, length),
                         Arguments.of(0x00, 0xFF, mode, cycles, 0xFF, true, false, length),
                         Arguments.of(0xFF, 0xFF, mode, cycles, 0x00, false, true, length),
                         Arguments.of(0xFF, 0x00, mode, cycles, 0xFF, true, false, length),
                         Arguments.of(0b1100, 0b0101, mode, cycles, 0b1001, false, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_EOR(int givenAccumulator,
                     int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedAccumulator,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        accumulator.store(Value.of(givenAccumulator));

        setNextOp(eor(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(Value.of(expectedAccumulator),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_INC()
    {
        return Stream.of(incInputs(modeAbsolute(), 3, 6),
                         incInputs(modeAbsoluteXSamePage(), 3, 6),
                         incInputs(modeAbsoluteXCrossPage(), 3, 7),
                         incInputs(modeAccumulator(), 1, 2),
                         incInputs(modeZeroPage(), 2, 5),
                         incInputs(modeZeroPageX(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> incInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0xFF, mode, cycles, 0x00, false, true, length),
                         Arguments.of(0x00, mode, cycles, 0x01, false, false, length),
                         Arguments.of(0x01, mode, cycles, 0x02, false, false, length),
                         Arguments.of(0xFE, mode, cycles, 0xFF, true, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_INC(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedOutput,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        ModeOutput modeOutput = modeGen.apply(modeInput(input));
        setNextOp(inc(modeOutput.mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> modeOutput.target().ifPresent(target -> verify(writer).write(target, Value.of(expectedOutput))),
                  () -> assertState(modeOutput.mode() instanceof Accumulator
                                    ? Value.of(expectedOutput)
                                    : state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        verify(writer).write(offsetLow(state.stackPointer(), -1), Value.of(0x36));
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    target,
                    offsetLow(state.stackPointer(), -2),
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
                    offsetLow(state.stackPointer(), -1),
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
                    offsetLow(state.stackPointer(), -1),
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
                    offsetLow(state.stackPointer(), -1),
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
                    offsetLow(state.stackPointer(), -1),
                    List.of(y.value()));
    }

    @ParameterizedTest
    @CsvSource({"00, false, true", "01, false, false", "FF, true, false"})
    void execute_PLA_Stack(String input, boolean isNegative, boolean isZero)
    {
        // given
        Value value = Value.ofHex(input);

        when(reader.read(offsetLow(stack.getPointer(), 1))).thenReturn(value);

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
                    offsetLow(state.stackPointer(), 1),
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

        when(reader.read(offsetLow(stack.getPointer(), 1))).thenReturn(value);

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
                    offsetLow(state.stackPointer(), 1),
                    List.of());
    }

    @ParameterizedTest
    @CsvSource({"00, false, true", "01, false, false", "FF, true, false"})
    void execute_PLX_Stack(String input, boolean isNegative, boolean isZero)
    {
        // given
        Value value = Value.ofHex(input);

        when(reader.read(offsetLow(stack.getPointer(), 1))).thenReturn(value);

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
                    offsetLow(state.stackPointer(), 1),
                    List.of());
    }

    @ParameterizedTest
    @CsvSource({"00, false, true", "01, false, false", "FF, true, false"})
    void execute_PLY_Stack(String input, boolean isNegative, boolean isZero)
    {
        // given
        Value value = Value.ofHex(input);

        when(reader.read(offsetLow(stack.getPointer(), 1))).thenReturn(value);

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
                    offsetLow(state.stackPointer(), 1),
                    List.of());
    }

    @ParameterizedTest
    @CsvSource({"0, 00000000, 00000000, 0, false, true",
                "0, 00000001, 00000000, 1, false, false",
                "1, 00000000, 10000000, 0, false, true",
                "1, 00000001, 10000000, 1, false, false",
                "0, 11111111, 01111111, 1, true, false"})
    void execute_ROL_Absolute(int expectedCarry,
                              String expected,
                              String memory,
                              int carry,
                              boolean isNegative,
                              boolean isZero)
    {
        // given
        status.setCarry(carry != 0);

        var address = Address.of(0x1234);
        when(reader.read(address)).thenReturn(Value.ofBits(memory));

        setNextOp(rol(absolute(address)));

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
                "0, 00000001, 00000000, 1, false, false",
                "1, 00000000, 10000000, 0, false, true",
                "1, 00000001, 10000000, 1, false, false",
                "0, 11111111, 01111111, 1, true, false"})
    void execute_ROL_Accumulator(int expectedCarry,
                                 String expected,
                                 String acc,
                                 int carry,
                                 boolean isNegative,
                                 boolean isZero)
    {
        // given
        accumulator.store(Value.ofBits(acc));
        status.setCarry(carry != 0);

        setNextOp(rol(accumulator()));

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
    void execute_RTI_Stack(String status,
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
        when(reader.read(offsetLow(stack.getPointer(), 1))).thenReturn(Value.ofBits(status));
        when(reader.read(offsetLow(stack.getPointer(), 2))).thenReturn(Value.of(0x34));
        when(reader.read(offsetLow(stack.getPointer(), 3))).thenReturn(Value.of(0x12));

        reset(clock);
        setNextOp(rti());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(6)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    new Flags(isNegative, isOverflow, isUser, isBreakCommand, isDecimal, isIrqDisable, isZero, isCarry),
                    Address.of(0x1234),
                    offsetLow(state.stackPointer(), 3),
                    List.of());
    }

    @Test
    void execute_RTS_Stack()
    {
        // given
        when(reader.read(offsetLow(stack.getPointer(), 1))).thenReturn(Value.of(0x33));
        when(reader.read(offsetLow(stack.getPointer(), 2))).thenReturn(Value.of(0x12));

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
                    offsetLow(state.stackPointer(), 2),
                    List.of());
    }

    @ParameterizedTest
    @CsvSource({"02, 03, 0, FE, true, false, false, false",
                "02, 03, 1, FF, true, false, false, false",
                "08, 01, 1, 07, false, false, false, true",
                "01, 01, 1, 00, false, false, true, true",
                "80, 01, 1, 7F, false, true, false, true"})
    void execute_SBC_Absolute(String acc,
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

        Address target = Address.of(0x1234);
        Value value = Value.ofHex(input);
        when(reader.read(target)).thenReturn(value);

        setNextOp(sbc(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        assertState(Value.ofHex(expected),
                    state.x(),
                    state.y(),
                    state.flags()
                         .toBuilder()
                         .negative(isNegative)
                         .overflow(isOverflow)
                         .zero(isZero).carry(isCarry).build(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"02, 03, 0, FE, true, false, false, false",
                "02, 03, 1, FF, true, false, false, false",
                "08, 01, 1, 07, false, false, false, true",
                "01, 01, 1, 00, false, false, true, true",
                "80, 01, 1, 7F, false, true, false, true"})
    void execute_SBC_Immediate(String acc,
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

        setNextOp(sbc(immediate(Value.ofHex(input))));

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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void execute_SEC_Implied(boolean carry)
    {
        // given
        status.setCarry(carry);

        setNextOp(sec());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().carry(true).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void execute_SED_Implied(boolean decimal)
    {
        // given
        status.setDecimal(decimal);

        setNextOp(sed());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().decimal(true).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void execute_SEI_Implied(boolean irqDisable)
    {
        // given
        status.setIrqDisable(irqDisable);

        setNextOp(sei());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(2)).nextCycle();
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags().toBuilder().irqDisable(true).build(),
                    state.programCounter().plus(Value.of(1)),
                    state.stackPointer(),
                    state.stackData());
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

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_STX_Absolute(String xVal)
    {
        // given
        x.store(Value.ofHex(xVal));

        var target = Address.of(0x1234);
        setNextOp(stx(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        verify(writer).write(target, Value.ofHex(xVal));
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0F", "FF"})
    void execute_STY_Absolute(String yVal)
    {
        // given
        y.store(Value.ofHex(yVal));

        var target = Address.of(0x1234);
        setNextOp(sty(absolute(target)));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        verify(clock, times(4)).nextCycle();
        verify(writer).write(target, Value.ofHex(yVal));
        assertState(state.accumulator(),
                    state.x(),
                    state.y(),
                    state.flags(),
                    state.programCounter().plus(Value.of(3)),
                    state.stackPointer(),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true", "01, 01, false, false", "FF, FF, true, false"})
    void execute_TAX_Implied(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(input));

        setNextOp(tax());

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
    @CsvSource({"00, 00, false, true", "01, 01, false, false", "FF, FF, true, false"})
    void execute_TAY_Implied(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        accumulator.store(Value.ofHex(input));

        setNextOp(tay());

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
    @CsvSource({"00, 00, false, true", "01, 01, false, false", "FF, FF, true, false"})
    void execute_TSX_Implied(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        stack.setPointer(Value.ofHex(input));

        setNextOp(tsx());

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
    @CsvSource({"00, 00, false, true", "01, 01, false, false", "FF, FF, true, false"})
    void execute_TXA_Implied(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        x.store(Value.ofHex(input));

        setNextOp(txa());

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

    @Test
    void execute_TXS_Implied()
    {
        // given
        x.store(Value.of(0x12));

        setNextOp(txs());

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
                    Address.of(Value.of(0x12), state.stackPointer().high()),
                    state.stackData());
    }

    @ParameterizedTest
    @CsvSource({"00, 00, false, true", "01, 01, false, false", "FF, FF, true, false"})
    void execute_TYA_Implied(String input, String expected, boolean isNegative, boolean isZero)
    {
        // given
        y.store(Value.ofHex(input));

        setNextOp(tya());

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

    private Address offsetLow(Address address, int offset)
    {
        return Address.of(address.low().plus(Value.of(offset)), address.high());
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

    record ModeInput(Value value, Reader reader, Register accumulator, Register x, Register y) {}

    record ModeOutput(AddressMode mode, Optional<Address> target) {}

    private ModeInput modeInput(int input)
    {
        return new ModeInput(Value.of(input), reader, accumulator, x, y);
    }

    private static Function<ModeInput, ModeOutput> modeAbsolute()
    {
        return (ModeInput input) -> {
            Address target = Address.of(0x1234);
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(absolute(target), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeAbsoluteXSamePage()
    {
        return (ModeInput input) -> {
            Address intermediate = Address.of(0x1234);

            Value offset = Value.of(0xFF);
            input.x().store(offset);

            Address target = intermediate.plus(offset);
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(absoluteX(intermediate), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeAbsoluteXCrossPage()
    {
        return (ModeInput input) -> {
            Address intermediate = Address.of(0x12FE);

            Value offset = Value.of(0x02);
            input.x().store(offset);

            Address target = intermediate.plus(offset);
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(absoluteX(intermediate), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeAbsoluteYSamePage()
    {
        return (ModeInput input) -> {
            Address intermediate = Address.of(0x1234);

            Value offset = Value.of(0xFF);
            input.y().store(offset);

            Address target = intermediate.plus(offset);
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(absoluteY(intermediate), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeAbsoluteYCrossPage()
    {
        return (ModeInput input) -> {
            Address intermediate = Address.of(0x12FE);

            Value offset = Value.of(0x02);
            input.y().store(offset);

            Address target = intermediate.plus(offset);
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(absoluteY(intermediate), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeAccumulator()
    {
        return (ModeInput input) -> {
            input.accumulator().store(input.value());
            return new ModeOutput(accumulator(), Optional.empty());
        };
    }

    private static Function<ModeInput, ModeOutput> modeImmediate()
    {
        return (ModeInput input) -> new ModeOutput(immediate(input.value()), Optional.empty());
    }

    private static Function<ModeInput, ModeOutput> modeZeroPage()
    {
        return (ModeInput input) -> {
            Value targetOffset = Value.of(0x12);

            Address target = Address.zeroPage(targetOffset);
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(zp(targetOffset), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeZeroPageXIndirect()
    {
        return (ModeInput input) -> {
            Value intermediateOffset = Value.of(0x12);

            Value offset = Value.of(0x02);
            input.x().store(offset);

            Address intermediate = Address.zeroPage(intermediateOffset.plus(offset));
            Address target = Address.of(0x1234);

            when(input.reader().read(Address.zeroPage(intermediateOffset))).thenReturn(Value.ZERO); // throwaway read
            when(input.reader().read(intermediate)).thenReturn(target.low());
            when(input.reader().read(intermediate.increment())).thenReturn(target.high());
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(zpXIndirect(intermediateOffset), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeZeroPageX()
    {
        return (ModeInput input) -> {
            Value intermediateOffset = Value.of(0x12);

            Value offset = Value.of(0x02);
            input.x().store(offset);

            Address target = Address.zeroPage(intermediateOffset.plus(offset));
            when(input.reader().read(Address.zeroPage(intermediateOffset))).thenReturn(Value.ZERO); // throwaway read
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(zpX(intermediateOffset), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeZeroPageIndirect()
    {
        return (ModeInput input) -> {
            Value offset = Value.of(0x12);

            Address intermediate = Address.zeroPage(offset);
            Address target = Address.of(0x1234);

            when(input.reader().read(intermediate)).thenReturn(target.low());
            when(input.reader().read(intermediate.increment())).thenReturn(target.high());
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(zpIndirect(offset), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeZeroPageIndirectYSamePage()
    {
        return (ModeInput input) -> {
            Value pointerOffset = Value.of(0x12);

            Address pointer = Address.zeroPage(pointerOffset);
            Address intermediate = Address.of(0x1232);

            when(input.reader().read(pointer)).thenReturn(intermediate.low());
            when(input.reader().read(pointer.increment())).thenReturn(intermediate.high());

            Value offset = Value.of(0xFF);
            input.y().store(offset);

            Address target = intermediate.plus(offset);
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(zpIndirectY(pointerOffset), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeZeroPageIndirectYCrossPage()
    {
        return (ModeInput input) -> {
            Value pointerOffset = Value.of(0x12);

            Address pointer = Address.zeroPage(pointerOffset);
            Address intermediate = Address.of(0x12FE);

            when(input.reader().read(pointer)).thenReturn(intermediate.low());
            when(input.reader().read(pointer.increment())).thenReturn(intermediate.high());

            Value offset = Value.of(0x02);
            input.y().store(offset);

            Address target = intermediate.plus(offset);
            when(input.reader().read(target)).thenReturn(input.value());

            return new ModeOutput(zpIndirectY(pointerOffset), Optional.of(target));
        };
    }
}
