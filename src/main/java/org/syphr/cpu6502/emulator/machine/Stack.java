package org.syphr.cpu6502.emulator.machine;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
class Stack
{
    @ToString.Include
    private final Deque<Value> data;
    private final Clock clock;

    public Stack(int size, Clock clock)
    {
        data = new LinkedBlockingDeque<>(size);
        this.clock = clock;
    }

    public void push(Value value)
    {
        clock.nextCycle();
        data.push(value);
        log.info("Value pushed to stack: {}", value);
    }

    public void pushAll(List<Value> values)
    {
        values.forEach(this::push);
    }

    public Value pop()
    {
        clock.nextCycle();
        Value value = data.pop();
        log.info("Value popped from stack: {}", value);
        return value;
    }

    public boolean isEmpty()
    {
        return data.isEmpty();
    }
}
