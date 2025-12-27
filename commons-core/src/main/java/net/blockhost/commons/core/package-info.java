/// Core utilities for 6b6t plugins.
///
/// This package provides shared utility classes that are commonly needed
/// across multiple plugins, such as time formatting, string manipulation,
/// and other general-purpose helpers.
/// ## Available Utilities
///
///     - [net.blockhost.commons.core.TimeFormatter] - Format and parse time durations
///
/// ## Time Formatting Examples
/// <pre>
/// `// Format seconds to human-readableTimeFormatter.formatSeconds(3661);// "1h 1m 1s"// Compact format for timersTimeFormatter.formatCompact(125);// "2:05"// Parse time stringsTimeFormatter.parseToSeconds("1h30m");// 5400`</pre>
///
/// @see net.blockhost.commons.core.TimeFormatter
@NullMarked
package net.blockhost.commons.core;

import org.jspecify.annotations.NullMarked;
