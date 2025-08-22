package org.syphr.cpu6502.emulator.machine;

public class HaltException extends RuntimeException
{
    public HaltException(String message)
    {
        super(message);
    }

    public HaltException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
