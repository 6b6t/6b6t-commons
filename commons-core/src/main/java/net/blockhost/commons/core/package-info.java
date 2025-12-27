/**
 * Core utilities for 6b6t plugins.
 *
 * <p>This package provides shared utility classes that are commonly needed
 * across multiple plugins, such as time formatting, string manipulation,
 * and other general-purpose helpers.
 *
 * <h2>Available Utilities</h2>
 *
 * <ul>
 *   <li>{@link net.blockhost.commons.core.TimeFormatter} - Format and parse time durations</li>
 * </ul>
 *
 * <h2>Time Formatting Examples</h2>
 *
 * <pre>{@code
 * // Format seconds to human-readable
 * TimeFormatter.formatSeconds(3661);  // "1h 1m 1s"
 *
 * // Compact format for timers
 * TimeFormatter.formatCompact(125);   // "2:05"
 *
 * // Parse time strings
 * TimeFormatter.parseToSeconds("1h30m");  // 5400
 * }</pre>
 *
 * @see net.blockhost.commons.core.TimeFormatter
 */
@NullMarked
package net.blockhost.commons.core;

import org.jspecify.annotations.NullMarked;
