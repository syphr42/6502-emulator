package org.syphr.emulator.cli.memory;

import org.syphr.emulator.cpu.Address;

import java.util.List;

public class Vectors extends ROM
{
    public Vectors(Address nmi, Address reset, Address irq)
    {
        super(Address.NMI, List.of(nmi.low(), nmi.high(), reset.low(), reset.high(), irq.low(), irq.high()));
    }
}
