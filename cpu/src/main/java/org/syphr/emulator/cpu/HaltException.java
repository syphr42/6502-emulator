package org.syphr.emulator.cpu;

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
