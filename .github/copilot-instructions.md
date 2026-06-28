# Copilot Instructions for 6502 Emulator

## Project Overview

This is a Java-based emulator for the WDC 65C02 8-bit microprocessor, along with supporting chip implementations (W65C22
VIA). The project uses Maven for build management and is organized as a multi-module structure.

### Module Architecture

- **`cpu`** - Core CPU emulator implementing the W65C02 instruction set
- **`via`** - W65C22 Versatile Interface Adapter chip emulation
- **`common`** - Low-level reusable components (Register, Value, Clock abstractions)
- **`cli`** - Command-line interface application using Spring Boot and Spring Shell

## Build, Test, and Lint Commands

### Full Build

```bash
mvn clean verify
```

### Build Target Module

```bash
mvn clean verify -pl cpu -am
mvn clean verify -pl cli -am
```

### Run Tests Only

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=CPUTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=CPUTest#execute_LDA
```

### Check License Headers

```bash
mvn license:check
```

### Format/Fix License Headers

Always run this after making any changes to source files to ensure license headers are present and correct.

```bash
mvn license:format
```

### Build CLI Application

```bash
mvn clean package -pl cli -am
```

## High-Level Architecture

### CPU Emulation Design

The CPU module implements the 6502 instruction set through several key components:

- **CPU** - Main processor class that orchestrates execution. Uses a builder pattern for construction (
  `CPU.builder().addressable(...).build()`).
- **Clock** - Manages clock cycles and notifies listeners of clock events
- **ALU** - Arithmetic Logic Unit for operations
- **InstructionDecoder** - Decodes 6502 opcodes into operations
- **StatusRegister** - 6502 status flags (NV-BDIZC)
- **Stack** - 6502 stack management (zero page at $0100-$01FF)
- **ProgramManager** - Program counter management
- **AddressMode** - Supports all 6502 addressing modes

### Listener/Event Pattern

The CPU uses a listener pattern for extensibility:

- **ClockListener** - Receives clock cycle events
- **OperationListener** - Receives instruction execution events
- **CPUEvent** - Event hierarchy (ClockCycleEvent, OperationEvent)
- **EventListenerList** - Swing's EventListenerList manages subscriptions

This allows external components (like the CLI, VIA chip, or monitoring tools) to observe CPU state changes without tight
coupling.

### Memory Interface

The **Addressable** interface provides the abstraction for memory access:

- **Reader** - Reads from addressable memory
- **Writer** - Writes to addressable memory

Components implement this interface to participate in the 6502 address bus (examples: RAM, ROM, memory-mapped I/O).

### Dependency Injection Strategy

The CLI module uses **Spring Boot** with component-based dependency injection:

- `@Component` - Marks Spring-managed beans
- `@SpringBootApplication` - Enables auto-configuration
- Spring Shell for command-line command registration
- Memory map with configurable segments (RAM, program ROM)

## Key Conventions

### Code Organization

- All source files must include Apache License 2.0 header (enforced by `maven-license-plugin`)
- Packages use `@NullMarked` from JSpecify for compile-time null-safety guarantees
- Use `package-info.java` files to declare package-level annotations

### Null Safety

This project uses **JSpecify** annotations for null-safety:

```java
@NullMarked  // In package-info.java - all types are non-null by default
package org.syphr.emulator.cpu;

import org.jspecify.annotations.NullMarked;
```

When a nullable field/parameter is needed, use `@Nullable`:

```java

@Nullable
private Reader reader;
```

### Builder Pattern

Use Lombok's `@Builder` or custom Builder classes for complex object construction:

```java
CPU cpu = CPU.builder()
             .addressable(memory)
             .start(Address.of(0x8000))
             .build();
```

### Testing Patterns

- **Test Framework**: JUnit 5 with parameterized tests
- **Assertions**: AssertJ (`assertThat(...)`)
- **Mocking**: Mockito with `@ExtendWith(MockitoExtension.class)`
- **Test Organization**: Place `@BeforeEach` setup to initialize mocks and get references to CPU state

Example:

```java

@ExtendWith(MockitoExtension.class)
class CPUTest
{
    @Mock
    Clock clock;
    @Mock
    Reader reader;
    @Mock
    Writer writer;

    @InjectMocks
    CPU cpu;

    @BeforeEach
    void beforeEach()
    {
        accumulator = cpu.getAccumulator();
        // ... get other state references
    }

    @ParameterizedTest
    @ValueSource(ints = {0xA9, 0xA5})
    void execute_LDA(int opcode)
    { ...}
}
```

### Lombok Usage

- `@RequiredArgsConstructor` - Constructor with required fields
- `@Getter(AccessLevel.PACKAGE)` - Package-private getters
- `@ToString(onlyExplicitlyIncluded = true)` - Selective toString output
- `@Builder(toBuilder = true)` - Builder and toBuilder() support

### Addressing and Values

- **Address** - 16-bit address abstraction (immutable)
- **Register** - 8-bit register abstraction
- **Value** - Represents 8-bit values with overflow handling (immutable)

Use factory methods: `Address.of(0x8000)`, `Value.of(0xFF)`

## Testing Architecture

ArchUnit is configured for architecture testing to ensure module boundaries are respected. Tests use the
`archunit-junit5` plugin.

## Java Version and Dependencies

- **Java**: 25
- **Spring Boot**
- **Spring Shell**
- **Junit**, **AssertJ**, **Mockito** for testing
- **Lombok** for code generation (optional dependency)
- **JSpecify** for null-safety

## Quick Tips

1. **Clock cycles matter** - The CPU is cycle-accurate; listener integration is essential for accurate timing
2. **EventListener usage** - Subscribe to CPU events to monitor execution without modifying CPU internals
3. **Memory mapping** - The CLI MemoryMap shows how address ranges are mapped to different storage types
4. **Instruction decode flow** - InstructionDecoder reads opcodes and routes to appropriate operation handlers
5. **Builder pattern is preferred** - Use builders for CPU and complex component construction
