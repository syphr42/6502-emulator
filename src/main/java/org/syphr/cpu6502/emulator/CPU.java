package org.syphr.cpu6502.emulator;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ToString
public class CPU
{
    private final Register a;

    public void execute(Program program)
    {
        program.operations().forEach(this::process);
    }

    private void process(Operation operation)
    {
        switch (operation) {
            case Operation.DEC _ -> a.decrement();
            case Operation.INC _ -> a.increment();
            case Operation.LDA lda -> a.store(lda.value());
        }
    }
}
