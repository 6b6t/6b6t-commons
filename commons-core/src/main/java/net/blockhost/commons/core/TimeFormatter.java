package net.blockhost.commons.core;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/// Utility class for formatting time durations into human-readable strings.
///
/// This class provides methods for converting durations into formatted strings
/// suitable for display to users, such as countdown timers, cooldown displays, etc.
///
/// Example usage:
/// <pre>
/// `// Format secondsTimeFormatter.formatSeconds(3661); // "1h 1m 1s"// Format durationTimeFormatter.format(Duration.ofMinutes(90)); // "1h 30m"// Compact formatTimeFormatter.formatCompact(Duration.ofSeconds(125)); // "2:05"`</pre>
public final class TimeFormatter {

    private TimeFormatter() {
        // Utility class
    }

    /// Formats a duration in seconds into a human-readable string.
    ///
    /// Format: "Xh Xm Xs" (e.g., "1h 30m 45s")
    /// Only non-zero units are included.
    ///
    /// @param totalSeconds the duration in seconds
    /// @return the formatted string
    public static String formatSeconds(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0s";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0 || sb.isEmpty()) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }

    /// Formats a Duration into a human-readable string.
    ///
    /// @param duration the duration to format
    /// @return the formatted string
    public static String format(Duration duration) {
        return formatSeconds(duration.toSeconds());
    }

    /// Formats a duration in milliseconds into a human-readable string.
    ///
    /// @param millis the duration in milliseconds
    /// @return the formatted string
    public static String formatMillis(long millis) {
        return formatSeconds(TimeUnit.MILLISECONDS.toSeconds(millis));
    }

    /// Formats a duration in seconds into a compact MM:SS or HH:MM:SS format.
    ///
    /// Examples:
    ///
    ///     - 65 seconds -&gt; "1:05"
    ///     - 3661 seconds -&gt; "1:01:01"
    ///
    ///
    /// @param totalSeconds the duration in seconds
    /// @return the formatted string
    public static String formatCompact(long totalSeconds) {
        if (totalSeconds < 0) {
            return "0:00";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
        }
    }

    /// Formats a Duration into a compact MM:SS or HH:MM:SS format.
    ///
    /// @param duration the duration to format
    /// @return the formatted string
    public static String formatCompact(Duration duration) {
        return formatCompact(duration.toSeconds());
    }

    /// Formats a duration with full unit names.
    ///
    /// Format: "X hours, X minutes, X seconds" (e.g., "1 hour, 30 minutes, 45 seconds")
    ///
    /// @param totalSeconds the duration in seconds
    /// @return the formatted string
    public static String formatLong(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0 seconds";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append(hours == 1 ? " hour" : " hours");
        }
        if (minutes > 0) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }
        if (seconds > 0 || sb.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }

        return sb.toString();
    }

    /// Formats a Duration with full unit names.
    ///
    /// @param duration the duration to format
    /// @return the formatted string
    public static String formatLong(Duration duration) {
        return formatLong(duration.toSeconds());
    }

    /// Parses a time string into seconds.
    ///
    /// Supported formats:
    ///
    ///     - "30" or "30s" - 30 seconds
    ///     - "5m" - 5 minutes (300 seconds)
    ///     - "2h" - 2 hours (7200 seconds)
    ///     - "1d" - 1 day (86400 seconds)
    ///     - "1h30m" - 1 hour 30 minutes
    ///
    ///
    /// @param input the time string to parse
    /// @return the duration in seconds
    /// @throws IllegalArgumentException if the input cannot be parsed
    public static long parseToSeconds(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input cannot be null or blank");
        }

        String trimmed = input.trim().toLowerCase(Locale.ROOT);
        long total = 0;

        StringBuilder number = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isDigit(c)) {
                number.append(c);
            } else if (Character.isLetter(c)) {
                if (number.isEmpty()) {
                    throw new IllegalArgumentException("Invalid time format: " + input);
                }
                long value = Long.parseLong(number.toString());
                number = new StringBuilder();

                total += switch (c) {
                    case 's' -> value;
                    case 'm' -> value * 60;
                    case 'h' -> value * 3600;
                    case 'd' -> value * 86400;
                    case 'w' -> value * 604800;
                    default -> throw new IllegalArgumentException("Unknown time unit: " + c);
                };
            }
        }

        // Handle trailing number (assumes seconds if no unit)
        if (!number.isEmpty()) {
            total += Long.parseLong(number.toString());
        }

        return total;
    }

    /// Parses a time string into a Duration.
    ///
    /// @param input the time string to parse
    /// @return the parsed Duration
    /// @throws IllegalArgumentException if the input cannot be parsed
    /// @see #parseToSeconds(String)
    public static Duration parse(String input) {
        return Duration.ofSeconds(parseToSeconds(input));
    }
}
