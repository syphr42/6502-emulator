package org.syphr.cpu6502.emulator.machine;

import lombok.ToString;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

@ToString
class Stack
{
    private final Deque<Value> data;

    public Stack(int size)
    {
        data = new LinkedBlockingDeque<>(size);
    }

    public void push(Value value)
    {
        data.push(value);
    }

    public void pushAll(List<Value> values)
    {
        values.forEach(this::push);
    }

    public Value pop()
    {
        return data.pop();
    }

    public boolean isEmpty()
    {
        return data.isEmpty();
    }
}
