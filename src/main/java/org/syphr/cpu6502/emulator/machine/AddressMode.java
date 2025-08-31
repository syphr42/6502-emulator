package org.syphr.cpu6502.emulator.machine;

public sealed interface AddressMode
{
    // @formatter:off
    record Absolute(Address address) implements AddressMode {}
    static Absolute absolute(Address address) { return new Absolute(address); }

    record AbsoluteIndexedXIndirect(Address address) implements AddressMode {}
    static AbsoluteIndexedXIndirect axi(Address address) { return new AbsoluteIndexedXIndirect(address); }

    record AbsoluteIndexedX(Address address) implements AddressMode {}
    static AbsoluteIndexedX ax(Address address) { return new AbsoluteIndexedX(address); }

    record AbsoluteIndexedY(Address address) implements AddressMode {}
    static AbsoluteIndexedY ay(Address address) { return new AbsoluteIndexedY(address); }

    record AbsoluteIndirect(Address address) implements AddressMode {}
    static AbsoluteIndirect ai(Address address) { return new AbsoluteIndirect(address); }

    record Accumulator() implements AddressMode {}
    static Accumulator accumulator() { return new Accumulator(); }

    record Immediate(Value value) implements AddressMode {}
    static Immediate immediate(Value value) { return new Immediate(value); }

    record Implied() implements AddressMode {}
    static Implied implied() { return new Implied(); }

    record Relative(Value displacement) implements AddressMode {}
    static Relative relative(Value displacement) { return new Relative(displacement); }

    record Stack() implements AddressMode {}
    static Stack stack() { return new Stack(); }

    record ZeroPage(Value offset) implements AddressMode {}
    static ZeroPage zp(Value offset) { return new ZeroPage(offset); }

    record ZeroPageIndexedXIndirect(Value offset) implements AddressMode {}
    static ZeroPageIndexedXIndirect zpxi(Value offset) { return new ZeroPageIndexedXIndirect(offset); }

    record ZeroPageIndexedX(Value offset) implements AddressMode {}
    static ZeroPageIndexedX zpx(Value offset) { return new ZeroPageIndexedX(offset); }

    record ZeroPageIndexedY(Value offset) implements AddressMode {}
    static ZeroPageIndexedY zpy(Value offset) { return new ZeroPageIndexedY(offset); }

    record ZeroPageIndirect(Value offset) implements AddressMode {}
    static ZeroPageIndirect zpi(Value offset) { return new ZeroPageIndirect(offset); }

    record ZeroPageIndirectIndexedY(Value offset) implements AddressMode {}
    static ZeroPageIndirectIndexedY zpiy(Value offset) { return new ZeroPageIndirectIndexedY(offset); }
    // @formatter:on
}
