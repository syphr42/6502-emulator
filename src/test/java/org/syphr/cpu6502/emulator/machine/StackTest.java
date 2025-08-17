package org.syphr.cpu6502.emulator.machine;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

class StackTest
{
    @Test
    void pop_StackEmpty_ThrowsException()
    {
        // given
        var stack = new Stack(1);

        // when
        Exception result = catchException(stack::pop);

        // then
        assertThat(result).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void pop_AfterPush_ReturnsOriginalValue()
    {
        // given
        var stack = new Stack(1);
        var value = Value.of(1);
        stack.push(value);

        // when
        Value result = stack.pop();

        // then
        assertThat(result).isSameAs(value);
    }

    @Test
    void push_ExceedsCapacity_ThrowsException()
    {
        // given
        var stack = new Stack(1);
        var value = Value.of(1);
        stack.push(value);

        // when
        Exception result = catchException(() -> stack.push(value));

        // then
        assertThat(result).isInstanceOf(IllegalStateException.class);
    }
}
