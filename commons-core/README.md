# commons-core

Core utilities for 6b6t plugins, providing essential helper classes used across all modules.

## Installation

```kotlin
dependencies {
    implementation("net.blockhost.commons:commons-core:1.0.0-SNAPSHOT")
}
```

## Features

### TimeFormatter

Comprehensive time formatting and parsing utilities supporting multiple output formats and input patterns.

#### Formatting Durations

```java
// Compact format (MM:SS or HH:MM:SS)
TimeFormatter.formatCompact(65);     // "1:05"
TimeFormatter.formatCompact(3661);   // "1:01:01"

// Standard format (Xh Xm Xs)
TimeFormatter.formatSeconds(3661);   // "1h 1m 1s"
TimeFormatter.format(Duration.ofMinutes(90)); // "1h 30m"

// Long format (full unit names)
TimeFormatter.formatLong(3661);      // "1 hour, 1 minute, 1 second"
TimeFormatter.formatLong(7200);      // "2 hours"

// From milliseconds
TimeFormatter.formatMillis(90000);   // "1m 30s"
```

#### Parsing Time Strings

```java
// Parse to seconds
TimeFormatter.parseToSeconds("30s");    // 30
TimeFormatter.parseToSeconds("5m");     // 300
TimeFormatter.parseToSeconds("2h");     // 7200
TimeFormatter.parseToSeconds("1d");     // 86400
TimeFormatter.parseToSeconds("1w");     // 604800
TimeFormatter.parseToSeconds("1h30m");  // 5400

// Parse to Duration
Duration duration = TimeFormatter.parse("2h30m");
duration.toMinutes(); // 150
```

#### Supported Time Units

| Unit | Suffix | Example |
|------|--------|---------|
| Seconds | `s` | `30s` |
| Minutes | `m` | `5m` |
| Hours | `h` | `2h` |
| Days | `d` | `1d` |
| Weeks | `w` | `1w` |

Combined units are also supported: `1h30m`, `2d12h`, `1w2d3h4m5s`

## API Documentation

Full Javadoc available at: **https://6b6t.github.io/6b6t-commons/**

## Related Modules

- [commons-config](../commons-config) - Configuration utilities
- [commons-message](../commons-message) - Message formatting
