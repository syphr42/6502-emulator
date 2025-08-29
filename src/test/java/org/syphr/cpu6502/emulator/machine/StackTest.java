package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StackTest
{
    @Mock
    Reader reader;

    @Mock
    Writer writer;

    @InjectMocks
    Stack stack;

    @Test
    void getPointer_InitialState_IsTop()
    {
        // when
        Address result = stack.getPointer();

        // then
        assertThat(result).isEqualTo(Address.of(0x01FF));
    }

    @Test
    void push_EmptyStack_WritesValueDecrementsPointer()
    {
        // given
        var value = Value.of(0x12);

        // when
        stack.push(value);

        // then
        verify(writer).write(Address.of(0x01FF), value);
        assertAll(() -> assertThat(stack.getPointer()).isEqualTo(Address.of(0x01FE)),
                  () -> assertThat(stack).extracting(Stack::isEmpty, Stack::isFull).containsExactly(false, false));
    }

    @Test
    void push_OneSlotOpen_ReportsNotFull()
    {
        // given
        IntStream.rangeClosed(0x00, 0xFE).mapToObj(Value::of).forEach(stack::push);

        // when
        boolean result = stack.isFull();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void push_Full_ReportsFull()
    {
        // given
        IntStream.rangeClosed(0x00, 0xFF).mapToObj(Value::of).forEach(stack::push);

        // when
        boolean result = stack.isFull();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void push_FullStack_OverwritesFirstValue()
    {
        // given
        IntStream.rangeClosed(0x00, 0xFF).mapToObj(Value::of).forEach(stack::push);
        reset(writer);

        var value = Value.of(0x12);

        // when
        stack.push(value);

        // then
        verify(writer).write(Address.of(0x01FF), value);
        assertAll(() -> assertThat(stack.getPointer()).isEqualTo(Address.of(0x01FE)),
                  () -> assertThat(stack).extracting(Stack::isEmpty, Stack::isFull).containsExactly(false, true));
    }

    @Test
    void pop_StackEmpty_ReadsAnyway()
    {
        // given
        Value value = Value.of(0x12);
        when(reader.read(Address.of(0x0100))).thenReturn(value);

        // when
        Value result = stack.pop();

        // then
        verify(reader).read(Address.of(0x0100));
        assertAll(() -> assertThat(result).isEqualTo(value),
                  () -> assertThat(stack.getPointer()).isEqualTo(Address.of(0x0100)),
                  () -> assertThat(stack).extracting(Stack::isEmpty, Stack::isFull).containsExactly(true, false));
    }

    @Test
    void pop_AfterPush_ReturnsOriginalValue()
    {
        // given
        var value = Value.of(0x12);
        stack.push(value);

        when(reader.read(Address.of(0x01FF))).thenReturn(value);

        // when
        Value result = stack.pop();

        // then
        verify(reader).read(Address.of(0x01FF));
        assertAll(() -> assertThat(result).isEqualTo(value),
                  () -> assertThat(stack.getPointer()).isEqualTo(Address.of(0x01FF)),
                  () -> assertThat(stack).extracting(Stack::isEmpty, Stack::isFull).containsExactly(true, false));
    }
}
