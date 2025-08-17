package org.syphr.cpu6502.emulator.machine;

import lombok.ToString;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

@ToString
public class Stack
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

    public Value pop()
    {
        return data.pop();
    }
}
