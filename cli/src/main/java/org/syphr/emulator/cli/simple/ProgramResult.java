package org.syphr.emulator.cli.simple;

import org.syphr.emulator.cpu.CPUState;

public record ProgramResult(CPUState initialState, CPUState finalState)
{
}
