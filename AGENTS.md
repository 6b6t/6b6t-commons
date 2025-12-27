# AGENTS.md - AI Coding Agent Guidelines

## Project Overview

6b6t-commons is a multi-module Gradle project with shared utilities for Minecraft plugins.
- **Group ID**: `net.blockhost.commons`
- **Java Version**: 25 (Temurin)
- **Modules**: commons-core, commons-command, commons-config, commons-database, commons-message

## Build Commands

```bash
./gradlew build                    # Build all modules
./gradlew test                     # Run all tests
./gradlew :commons-database:test   # Run tests for specific module
./gradlew check                    # Run tests + static analysis

# Run a single test class
./gradlew :commons-database:test --tests "net.blockhost.commons.database.DatabaseCredentialsTest"

# Run a single test method
./gradlew :commons-database:test --tests "net.blockhost.commons.database.DatabaseCredentialsTest.testBuilder"

./gradlew spotlessApply            # Format code (auto-fix)
./gradlew spotlessCheck            # Check code formatting
./gradlew aggregateJavadoc         # Generate combined Javadoc
./gradlew rewriteRun               # Apply OpenRewrite recipes
./gradlew rewriteDryRun            # Preview OpenRewrite changes
```

## Code Style Guidelines

### Formatting (enforced by Spotless)

- **Formatter**: Palantir Java Format 2.82.0
- **Indentation**: 4 spaces (no tabs)
- **Max line length**: 120 characters
- **Continuation indent**: 8 spaces
- **Line endings**: LF (Unix)
- **Trailing whitespace**: Trimmed
- **Final newline**: Required

### Import Order

1. All other imports (default)
2. `java.*` and `javax.*` imports
3. Static imports

```java
import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

import static java.util.Collections.emptyList;
```

### Naming Conventions

- **Classes**: PascalCase (`MessageService`, `HikariDataSourceBuilder`)
- **Methods**: camelCase (`createDataSource`, `findSubCommand`)
- **Constants**: UPPER_SNAKE_CASE (`DRIVER_CLASS`, `MINI_MESSAGE`)
- **Fields/Parameters**: camelCase (`poolName`, `maximumPoolSize`)

### Type Annotations

Use JetBrains nullability annotations:
```java
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public @NotNull Optional<SubCommand> findSubCommand(@NotNull String name) { ... }
public Builder password(@Nullable String password) { ... }
```

For package-level null safety, use JSpecify in `package-info.java`:
```java
@NullMarked
package net.blockhost.commons.database;

import org.jspecify.annotations.NullMarked;
```

### Documentation Style

Use Java 23+ Markdown doc comments (`///`). Link classes with `[ClassName]`:
```java
/// Creates a new builder with the specified [DatabaseCredentials].
///
/// @param credentials the database credentials
/// @return a new builder instance
public static HikariDataSourceBuilder create(DatabaseCredentials credentials) {
```

### Builder Pattern

Use for complex objects with private constructor:
```java
public final class DatabaseCredentials {
    private DatabaseCredentials(Builder builder) { ... }
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Builder() {}
        public Builder host(String host) {
            this.host = Objects.requireNonNull(host, "host");
            return this;
        }
        public DatabaseCredentials build() { return new DatabaseCredentials(this); }
    }
}
```

### Error Handling

- Use `Objects.requireNonNull()` for parameter validation
- Throw `IllegalArgumentException` for invalid values with descriptive messages
- Return error components instead of throwing for missing config:
```java
if (raw == null) return Component.text("<Missing message: " + key + ">");
```

### Immutability

Prefer immutable classes with `final` fields. Return unmodifiable collections:
```java
return Collections.unmodifiableMap(new LinkedHashMap<>(builder.properties));
return List.copyOf(subCommands);
```

## Static Analysis

- **SpotBugs**: Finds bugs; fails build on issues (exclusions in `config/spotbugs/exclude.xml`)
- **Error Prone**: Catches common Java mistakes at compile time
- **OpenRewrite**: Automated code refactoring recipes

## Testing

- **Framework**: JUnit 5 (Jupiter) with Mockito
- **Test class naming**: `*Test.java`

## Lombok

Available but used sparingly. Null annotations use JetBrains (`lombok.config`).

## Dependencies

Key deps (versions in `gradle/libs.versions.toml`):
- HikariCP, ConfigLib, Adventure API, Paper API / Velocity API

## CI/CD

GitHub Actions (`.github/workflows/publish.yml`): builds with JDK 25, publishes to GitHub Packages, deploys Javadoc to GitHub Pages on push to main/master or version tags.
