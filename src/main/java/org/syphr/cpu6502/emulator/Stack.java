package org.syphr.cpu6502.emulator;

import lombok.ToString;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

@ToString
public class Stack
{
    private final Deque<Byte> data;

    public Stack(int size)
    {
        data = new LinkedBlockingDeque<>(size);
    }

    public void push(byte b)
    {
        data.push(b);
    }

    public byte pop()
    {
        return data.pop();
    }
}
