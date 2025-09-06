package org.syphr.emulator.cpu;

import org.jspecify.annotations.Nullable;
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
import static org.syphr.emulator.cpu.AddressMode.*;
import static org.syphr.emulator.cpu.Operation.*;

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
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    Address.ofHex(expectedPC),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().carry(false).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().decimal(false).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().irqDisable(false).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().overflow(false).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    Value.ofHex(expected),
                                    state.y(),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    Value.ofHex(expected),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
    void execute_INX_Implied(String xVal, String expected, boolean isNegative, boolean isZero)
    {
        // given
        x.store(Value.ofHex(xVal));

        setNextOp(inx());

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    Value.ofHex(expected),
                                    state.y(),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    Value.ofHex(expected),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_JMP()
    {
        return Stream.of(Arguments.of((Function<ModeInput, ModeOutput>) (ModeInput _) -> {
            Address target = Address.of(0x1234);
            return new ModeOutput(absolute(target), Optional.of(target));
        }, 3), Arguments.of((Function<ModeInput, ModeOutput>) (ModeInput input) -> {
            Address intermediate = Address.of(0x4321);

            Value offset = Value.of(0xFF);
            input.x().store(offset);

            Address pointer = intermediate.plusUnsigned(offset);
            Address target = Address.of(0x1234);
            when(input.reader().read(pointer)).thenReturn(target.low());
            when(input.reader().read(pointer.increment())).thenReturn(target.high());

            return new ModeOutput(absoluteXIndirect(intermediate), Optional.of(target));
        }, 6), Arguments.of((Function<ModeInput, ModeOutput>) (ModeInput input) -> {
            Address intermediate = Address.of(0x4321);

            Address target = Address.of(0x1234);
            when(input.reader().read(intermediate)).thenReturn(target.low());
            when(input.reader().read(intermediate.increment())).thenReturn(target.high());

            return new ModeOutput(absoluteIndirect(intermediate), Optional.of(target));
        }, 6));
    }

    @ParameterizedTest
    @MethodSource
    void execute_JMP(Function<ModeInput, ModeOutput> modeGen, int expectedCycles)
    {
        // given
        ModeOutput modeOutput = modeGen.apply(modeInput(0));
        setNextOp(jmp(modeOutput.mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    modeOutput.target().orElseThrow(),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(6)).nextCycle(),
                  () -> verify(writer).write(state.stackPointer(), Value.of(0x12)),
                  () -> verify(writer).write(offsetLow(state.stackPointer(), -1), Value.of(0x36)),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    target,
                                    offsetLow(state.stackPointer(), -2),
                                    List.of(Value.of(0x36), Value.of(0x12))));
    }

    static Stream<Arguments> execute_LDA()
    {
        return Stream.of(ldaInputs(modeAbsolute(), 3, 4),
                         ldaInputs(modeAbsoluteXSamePage(), 3, 4),
                         ldaInputs(modeAbsoluteXCrossPage(), 3, 5),
                         ldaInputs(modeAbsoluteYSamePage(), 3, 4),
                         ldaInputs(modeAbsoluteYCrossPage(), 3, 5),
                         ldaInputs(modeImmediate(), 2, 2),
                         ldaInputs(modeZeroPage(), 2, 3),
                         ldaInputs(modeZeroPageXIndirect(), 2, 6),
                         ldaInputs(modeZeroPageX(), 2, 4),
                         ldaInputs(modeZeroPageIndirect(), 2, 5),
                         ldaInputs(modeZeroPageIndirectYSamePage(), 2, 5),
                         ldaInputs(modeZeroPageIndirectYCrossPage(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> ldaInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, mode, cycles, false, true, length),
                         Arguments.of(0x01, mode, cycles, false, false, length),
                         Arguments.of(0xFF, mode, cycles, true, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_LDA(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        setNextOp(lda(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(Value.of(input),
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

    static Stream<Arguments> execute_LDX()
    {
        return Stream.of(ldxInputs(modeAbsolute(), 3, 4),
                         ldxInputs(modeAbsoluteYSamePage(), 3, 4),
                         ldxInputs(modeAbsoluteYCrossPage(), 3, 5),
                         ldxInputs(modeImmediate(), 2, 2),
                         ldxInputs(modeZeroPage(), 2, 3),
                         ldxInputs(modeZeroPageY(), 2, 4)).flatMap(i -> i);
    }

    static Stream<Arguments> ldxInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, mode, cycles, false, true, length),
                         Arguments.of(0x01, mode, cycles, false, false, length),
                         Arguments.of(0xFF, mode, cycles, true, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_LDX(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        setNextOp(ldx(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    Value.of(input),
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

    static Stream<Arguments> execute_LDY()
    {
        return Stream.of(ldyInputs(modeAbsolute(), 3, 4),
                         ldyInputs(modeAbsoluteXSamePage(), 3, 4),
                         ldyInputs(modeAbsoluteXCrossPage(), 3, 5),
                         ldyInputs(modeImmediate(), 2, 2),
                         ldyInputs(modeZeroPage(), 2, 3),
                         ldyInputs(modeZeroPageX(), 2, 4)).flatMap(i -> i);
    }

    static Stream<Arguments> ldyInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, mode, cycles, false, true, length),
                         Arguments.of(0x01, mode, cycles, false, false, length),
                         Arguments.of(0xFF, mode, cycles, true, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_LDY(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        setNextOp(ldy(modeGen.apply(modeInput(input)).mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    Value.of(input),
                                    state.flags()
                                         .toBuilder()
                                         .negative(expectedNegative)
                                         .zero(expectedZero)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_LSR()
    {
        return Stream.of(lsrInputs(modeAbsolute(), 3, 6),
                         lsrInputs(modeAbsoluteXSamePage(), 3, 6),
                         lsrInputs(modeAbsoluteXCrossPage(), 3, 7),
                         lsrInputs(modeAccumulator(), 1, 2),
                         lsrInputs(modeZeroPage(), 2, 5),
                         lsrInputs(modeZeroPageX(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> lsrInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0b00000000, mode, cycles, 0b00000000, true, false, length),
                         Arguments.of(0b10000000, mode, cycles, 0b01000000, false, false, length),
                         Arguments.of(0b00000001, mode, cycles, 0b00000000, true, true, length),
                         Arguments.of(0b01010101, mode, cycles, 0b00101010, false, true, length),
                         Arguments.of(0b10101010, mode, cycles, 0b01010101, false, false, length),
                         Arguments.of(0b00000011, mode, cycles, 0b00000001, false, true, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_LSR(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedOutput,
                     boolean expectedZero,
                     boolean expectedCarry,
                     int expectedProgramCounterOffset)
    {
        // given
        ModeOutput modeOutput = modeGen.apply(modeInput(input));
        setNextOp(lsr(modeOutput.mode()));

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
                                         .zero(expectedZero)
                                         .carry(expectedCarry)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
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

    static Stream<Arguments> execute_ORA()
    {
        return Stream.of(oraInputs(modeAbsolute(), 3, 4),
                         oraInputs(modeAbsoluteXSamePage(), 3, 4),
                         oraInputs(modeAbsoluteXCrossPage(), 3, 5),
                         oraInputs(modeAbsoluteYSamePage(), 3, 4),
                         oraInputs(modeAbsoluteYCrossPage(), 3, 5),
                         oraInputs(modeImmediate(), 2, 2),
                         oraInputs(modeZeroPage(), 2, 3),
                         oraInputs(modeZeroPageXIndirect(), 2, 6),
                         oraInputs(modeZeroPageX(), 2, 4),
                         oraInputs(modeZeroPageIndirect(), 2, 5),
                         oraInputs(modeZeroPageIndirectYSamePage(), 2, 5),
                         oraInputs(modeZeroPageIndirectYCrossPage(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> oraInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, 0x00, mode, cycles, 0x00, false, true, length),
                         Arguments.of(0x00, 0xFF, mode, cycles, 0xFF, true, false, length),
                         Arguments.of(0xFF, 0xFF, mode, cycles, 0xFF, true, false, length),
                         Arguments.of(0xFF, 0x00, mode, cycles, 0xFF, true, false, length),
                         Arguments.of(0b1100, 0b0101, mode, cycles, 0b1101, false, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_ORA(int givenAccumulator,
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

        setNextOp(ora(modeGen.apply(modeInput(input)).mode()));

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
        assertAll(() -> verify(clock, times(3)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    state.programCounter().plus(Value.of(1)),
                                    offsetLow(state.stackPointer(), -1),
                                    List.of(accumulator.value())));
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
        assertAll(() -> verify(clock, times(3)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    state.programCounter().plus(Value.of(1)),
                                    offsetLow(state.stackPointer(), -1),
                                    List.of(status.value())));
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
        assertAll(() -> verify(clock, times(3)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    state.programCounter().plus(Value.of(1)),
                                    offsetLow(state.stackPointer(), -1),
                                    List.of(x.value())));
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
        assertAll(() -> verify(clock, times(3)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    state.programCounter().plus(Value.of(1)),
                                    offsetLow(state.stackPointer(), -1),
                                    List.of(y.value())));
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
        assertAll(() -> verify(clock, times(4)).nextCycle(),
                  () -> assertState(value,
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    offsetLow(state.stackPointer(), 1),
                                    List.of()));
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
        assertAll(() -> verify(clock, times(4)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    new Flags(isNegative,
                                              isOverflow,
                                              isUser,
                                              isBreakCommand,
                                              isDecimal,
                                              isIrqDisable,
                                              isZero,
                                              isCarry),
                                    state.programCounter().plus(Value.of(1)),
                                    offsetLow(state.stackPointer(), 1),
                                    List.of()));
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
        assertAll(() -> verify(clock, times(4)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    value,
                                    state.y(),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    offsetLow(state.stackPointer(), 1),
                                    List.of()));
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
        assertAll(() -> verify(clock, times(4)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    value,
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    offsetLow(state.stackPointer(), 1),
                                    List.of()));
    }

    static Stream<Arguments> execute_ROL()
    {
        return Stream.of(rolInputs(modeAbsolute(), 3, 6),
                         rolInputs(modeAbsoluteXSamePage(), 3, 6),
                         rolInputs(modeAbsoluteXCrossPage(), 3, 7),
                         rolInputs(modeAccumulator(), 1, 2),
                         rolInputs(modeZeroPage(), 2, 5),
                         rolInputs(modeZeroPageX(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> rolInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0, 0b00000000, 0b00000000, 0, mode, cycles, false, true, length),
                         Arguments.of(0, 0b00000001, 0b00000000, 1, mode, cycles, false, false, length),
                         Arguments.of(1, 0b00000000, 0b10000000, 0, mode, cycles, false, true, length),
                         Arguments.of(1, 0b00000001, 0b10000000, 1, mode, cycles, false, false, length),
                         Arguments.of(0, 0b11111111, 0b01111111, 1, mode, cycles, true, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_ROL(int expectedCarry,
                     int expectedOutput,
                     int input,
                     int givenCarry,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        status.setCarry(givenCarry != 0);

        ModeOutput modeOutput = modeGen.apply(modeInput(input));
        setNextOp(rol(modeOutput.mode()));

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
                                         .carry(expectedCarry != 0)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_ROR()
    {
        return Stream.of(rorInputs(modeAbsolute(), 3, 6),
                         rorInputs(modeAbsoluteXSamePage(), 3, 6),
                         rorInputs(modeAbsoluteXCrossPage(), 3, 7),
                         rorInputs(modeAccumulator(), 1, 2),
                         rorInputs(modeZeroPage(), 2, 5),
                         rorInputs(modeZeroPageX(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> rorInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0, 0b00000000, 0b00000000, 0, mode, cycles, false, true, length),
                         Arguments.of(0, 0b00000001, 0b00000000, 1, mode, cycles, false, true, length),
                         Arguments.of(1, 0b00000000, 0b10000000, 0, mode, cycles, true, false, length),
                         Arguments.of(1, 0b00000001, 0b10000000, 1, mode, cycles, true, false, length),
                         Arguments.of(0, 0b11111111, 0b01111111, 1, mode, cycles, false, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_ROR(int givenCarry,
                     int input,
                     int expectedOutput,
                     int expectedCarry,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     boolean expectedNegative,
                     boolean expectedZero,
                     int expectedProgramCounterOffset)
    {
        // given
        status.setCarry(givenCarry != 0);

        ModeOutput modeOutput = modeGen.apply(modeInput(input));
        setNextOp(ror(modeOutput.mode()));

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
                                         .carry(expectedCarry != 0)
                                         .build(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(6)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    new Flags(isNegative,
                                              isOverflow,
                                              isUser,
                                              isBreakCommand,
                                              isDecimal,
                                              isIrqDisable,
                                              isZero,
                                              isCarry),
                                    Address.of(0x1234),
                                    offsetLow(state.stackPointer(), 3),
                                    List.of()));
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
        assertAll(() -> verify(clock, times(6)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    Address.of(0x1234),
                                    offsetLow(state.stackPointer(), 2),
                                    List.of()));
    }

    static Stream<Arguments> execute_SBC()
    {
        return Stream.of(sbcInputs(modeAbsolute(), 3, 4),
                         sbcInputs(modeAbsoluteXSamePage(), 3, 4),
                         sbcInputs(modeAbsoluteXCrossPage(), 3, 5),
                         sbcInputs(modeAbsoluteYSamePage(), 3, 4),
                         sbcInputs(modeAbsoluteYCrossPage(), 3, 5),
                         sbcInputs(modeImmediate(), 2, 2),
                         sbcInputs(modeZeroPage(), 2, 3),
                         sbcInputs(modeZeroPageXIndirect(), 2, 6),
                         sbcInputs(modeZeroPageX(), 2, 4),
                         sbcInputs(modeZeroPageIndirect(), 2, 5),
                         sbcInputs(modeZeroPageIndirectYSamePage(), 2, 5),
                         sbcInputs(modeZeroPageIndirectYCrossPage(), 2, 6))
                     .flatMap(i -> i);
    }

    static Stream<Arguments> sbcInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x02, 0, 0x03, mode, cycles, 0xFE, true, false, false, false, length),
                         Arguments.of(0x02, 1, 0x03, mode, cycles, 0xFF, true, false, false, false, length),
                         Arguments.of(0x08, 1, 0x01, mode, cycles, 0x07, false, false, false, true, length),
                         Arguments.of(0x01, 1, 0x01, mode, cycles, 0x00, false, false, true, true, length),
                         Arguments.of(0x80, 1, 0x01, mode, cycles, 0x7F, false, true, false, true, length),
                         Arguments.of(0x7F, 1, 0xFF, mode, cycles, 0x80, true, true, false, false, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_SBC(int givenAccumulator,
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

        setNextOp(sbc(modeGen.apply(modeInput(input)).mode()));

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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().carry(true).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().decimal(true).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().irqDisable(true).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_STA()
    {
        return Stream.of(staInputs(modeAbsolute(), 3, 4),
                         staInputs(modeAbsoluteXSamePage(), 3, 4),
                         staInputs(modeAbsoluteXCrossPage(), 3, 5),
                         staInputs(modeAbsoluteYSamePage(), 3, 4),
                         staInputs(modeAbsoluteYCrossPage(), 3, 5),
                         staInputs(modeZeroPage(), 2, 3),
                         staInputs(modeZeroPageXIndirect(), 2, 6),
                         staInputs(modeZeroPageX(), 2, 4),
                         staInputs(modeZeroPageIndirect(), 2, 5),
                         staInputs(modeZeroPageIndirectYSamePage(), 2, 5),
                         staInputs(modeZeroPageIndirectYCrossPage(), 2, 6)).flatMap(i -> i);
    }

    static Stream<Arguments> staInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, mode, cycles, length),
                         Arguments.of(0x01, mode, cycles, length),
                         Arguments.of(0xFF, mode, cycles, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_STA(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedProgramCounterOffset)
    {
        // given
        var value = Value.of(input);
        accumulator.store(value);

        ModeOutput modeOutput = modeGen.apply(modeInput());
        setNextOp(sta(modeOutput.mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> verify(writer).write(modeOutput.target().orElseThrow(), value),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_STX()
    {
        return Stream.of(stxInputs(modeAbsolute(), 3, 4),
                         stxInputs(modeZeroPage(), 2, 3),
                         stxInputs(modeZeroPageY(), 2, 4)).flatMap(i -> i);
    }

    static Stream<Arguments> stxInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, mode, cycles, length),
                         Arguments.of(0x01, mode, cycles, length),
                         Arguments.of(0xFF, mode, cycles, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_STX(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedProgramCounterOffset)
    {
        // given
        var value = Value.of(input);
        x.store(value);

        ModeOutput modeOutput = modeGen.apply(modeInput());
        setNextOp(stx(modeOutput.mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> verify(writer).write(modeOutput.target().orElseThrow(), value),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
    }

    static Stream<Arguments> execute_STY()
    {
        return Stream.of(styInputs(modeAbsolute(), 3, 4),
                         styInputs(modeZeroPage(), 2, 3),
                         styInputs(modeZeroPageX(), 2, 4)).flatMap(i -> i);
    }

    static Stream<Arguments> styInputs(Function<ModeInput, ModeOutput> mode, int length, int cycles)
    {
        return Stream.of(Arguments.of(0x00, mode, cycles, length),
                         Arguments.of(0x01, mode, cycles, length),
                         Arguments.of(0xFF, mode, cycles, length));
    }

    @ParameterizedTest
    @MethodSource
    void execute_STY(int input,
                     Function<ModeInput, ModeOutput> modeGen,
                     int expectedCycles,
                     int expectedProgramCounterOffset)
    {
        // given
        var value = Value.of(input);
        y.store(value);

        ModeOutput modeOutput = modeGen.apply(modeInput());
        setNextOp(sty(modeOutput.mode()));

        // when
        CPUState state = cpu.getState();
        cpu.executeNext();

        // then
        assertAll(() -> verify(clock, times(expectedCycles)).nextCycle(),
                  () -> verify(writer).write(modeOutput.target().orElseThrow(), value),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    state.programCounter().plus(Value.of(expectedProgramCounterOffset)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    Value.ofHex(expected),
                                    state.y(),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    Value.ofHex(expected),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    Value.ofHex(expected),
                                    state.y(),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(Value.ofHex(expected),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(state.accumulator(),
                                    state.x(),
                                    state.y(),
                                    state.flags(),
                                    state.programCounter().plus(Value.of(1)),
                                    Address.of(Value.of(0x12), state.stackPointer().high()),
                                    state.stackData()));
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
        assertAll(() -> verify(clock, times(2)).nextCycle(),
                  () -> assertState(Value.ofHex(expected),
                                    state.x(),
                                    state.y(),
                                    state.flags().toBuilder().negative(isNegative).zero(isZero).build(),
                                    state.programCounter().plus(Value.of(1)),
                                    state.stackPointer(),
                                    state.stackData()));
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

    record ModeInput(@Nullable Value value, Reader reader, Register accumulator, Register x, Register y) {}

    record ModeOutput(AddressMode mode, Optional<Address> target) {}

    private ModeInput modeInput(int input)
    {
        return new ModeInput(Value.of(input), reader, accumulator, x, y);
    }

    private ModeInput modeInput()
    {
        return new ModeInput(null, reader, accumulator, x, y);
    }

    private static Function<ModeInput, ModeOutput> modeAbsolute()
    {
        return (ModeInput input) -> {
            Address target = Address.of(0x1234);
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

            return new ModeOutput(absoluteY(intermediate), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeAccumulator()
    {
        return (ModeInput input) -> {
            if (input.value() != null) {
                input.accumulator().store(input.value());
            }
            return new ModeOutput(accumulator(), Optional.empty());
        };
    }

    private static Function<ModeInput, ModeOutput> modeImmediate()
    {
        return (ModeInput input) -> {
            if (input.value() == null) {
                throw new IllegalArgumentException("Input cannot be null for immediate mode");
            }
            return new ModeOutput(immediate(input.value()), Optional.empty());
        };
    }

    private static Function<ModeInput, ModeOutput> modeZeroPage()
    {
        return (ModeInput input) -> {
            Value targetOffset = Value.of(0x12);

            Address target = Address.zeroPage(targetOffset);
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

            return new ModeOutput(zpX(intermediateOffset), Optional.of(target));
        };
    }

    private static Function<ModeInput, ModeOutput> modeZeroPageY()
    {
        return (ModeInput input) -> {
            Value intermediateOffset = Value.of(0x12);

            Value offset = Value.of(0x02);
            input.y().store(offset);

            Address target = Address.zeroPage(intermediateOffset.plus(offset));
            when(input.reader().read(Address.zeroPage(intermediateOffset))).thenReturn(Value.ZERO); // throwaway read
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

            return new ModeOutput(zpY(intermediateOffset), Optional.of(target));
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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

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
            if (input.value() != null) {
                when(input.reader().read(target)).thenReturn(input.value());
            }

            return new ModeOutput(zpIndirectY(pointerOffset), Optional.of(target));
        };
    }
}
