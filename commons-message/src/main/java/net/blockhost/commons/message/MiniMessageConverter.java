package net.blockhost.commons.message;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/// Utility class for converting legacy Minecraft color codes to MiniMessage format.
///
/// This converter handles both section sign (§) and ampersand (&) color codes,
/// including standard colors, formatting codes, and hex colors in various formats.
///
/// ## Supported Formats
///
/// | Format | Example | Description |
/// |--------|---------|-------------|
/// | Standard colors | `&a`, `§c` | Legacy color codes 0-9 and a-f |
/// | Formatting | `&l`, `§o` | Bold, italic, underline, etc. |
/// | Hex (short) | `&#FF0000`, `§#FF0000` | 6-digit hex colors |
/// | Hex (ugly) | `&x&F&F&0&0&0&0` | Spigot-style hex format |
///
/// ## Example Usage
///
/// ```java
/// // Convert legacy colors to MiniMessage
/// String legacy = "&aHello &lWorld!";
/// String miniMessage = MiniMessageConverter.convert(legacy);
/// // Result: "<reset><green>Hello <bold>World!"
///
/// // Then parse with MiniMessage
/// Component component = MessageService.parse(miniMessage);
/// ```
///
/// @see MessageService
public final class MiniMessageConverter {
    private static final Map<String, String> COLOR_MAP = Map.ofEntries(
            Map.entry("0", "black"),
            Map.entry("1", "dark_blue"),
            Map.entry("2", "dark_green"),
            Map.entry("3", "dark_aqua"),
            Map.entry("4", "dark_red"),
            Map.entry("5", "dark_purple"),
            Map.entry("6", "gold"),
            Map.entry("7", "gray"),
            Map.entry("8", "dark_gray"),
            Map.entry("9", "blue"),
            Map.entry("a", "green"),
            Map.entry("b", "aqua"),
            Map.entry("c", "red"),
            Map.entry("d", "light_purple"),
            Map.entry("e", "yellow"),
            Map.entry("f", "white"));

    private static final Map<String, String> FORMATTING_MAP = Map.ofEntries(
            Map.entry("k", "obfuscated"),
            Map.entry("l", "bold"),
            Map.entry("m", "strikethrough"),
            Map.entry("n", "underlined"),
            Map.entry("o", "italic"),
            Map.entry("r", "reset"));

    private static final Pattern SECTION_HEX_PATTERN = Pattern.compile("§#([0-9a-fA-F]{6})");
    private static final Pattern AMPERSAND_HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern SECTION_HEX_UGLY_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}");
    private static final Pattern AMPERSAND_HEX_UGLY_PATTERN = Pattern.compile("&x(&[0-9a-fA-F]){6}");

    private MiniMessageConverter() {}

    /// Converts a string containing legacy color codes to MiniMessage format.
    ///
    /// This method handles both ampersand (&) and section sign (§) color codes,
    /// converting them to their MiniMessage tag equivalents.
    ///
    /// @param str the string containing legacy color codes
    /// @return the string with legacy codes converted to MiniMessage tags
    public static String convert(String str) {
        str = convertLegacyCodes(str, '&', AMPERSAND_HEX_PATTERN, AMPERSAND_HEX_UGLY_PATTERN);
        str = convertLegacyCodes(str, '§', SECTION_HEX_PATTERN, SECTION_HEX_UGLY_PATTERN);
        return str;
    }

    private static String convertLegacyCodes(String str, char legacyChar, Pattern hexPattern, Pattern hexUglyPattern) {
        {
            // Convert hex colors
            var hexMatcher = hexPattern.matcher(str);
            StringBuilder sb = new StringBuilder();
            while (hexMatcher.find()) {
                String hex = hexMatcher.group(1).toUpperCase(Locale.ROOT);
                hexMatcher.appendReplacement(sb, "<reset><#" + hex + ">");
            }
            hexMatcher.appendTail(sb);
            str = sb.toString();
        }

        {
            // Convert ugly hex colors (handles patterns like &x&F&F&0&0&0&0 or §x§F§F§0§0§0§0)
            var uglyHexMatcher = hexUglyPattern.matcher(str);
            StringBuilder sb = new StringBuilder();
            while (uglyHexMatcher.find()) {
                StringBuilder hex = new StringBuilder();
                String match = uglyHexMatcher.group();
                // Collect only hexadecimal characters from the match (0-9, a-f, A-F)
                for (int i = 0; i < match.length(); i++) {
                    char c = match.charAt(i);
                    if (Character.digit(c, 16) != -1) {
                        hex.append(c);
                    }
                }
                uglyHexMatcher.appendReplacement(
                        sb, "<reset><#" + hex.toString().toUpperCase(Locale.ROOT) + ">");
            }
            uglyHexMatcher.appendTail(sb);
            str = sb.toString();
        }

        // Convert standard colors
        StringBuilder result = new StringBuilder();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == legacyChar && i + 1 < chars.length) {
                String code = String.valueOf(chars[i + 1]).toLowerCase(Locale.ROOT);
                String colorReplacement = COLOR_MAP.get(code);
                if (colorReplacement != null) {
                    result.append("<reset>")
                            .append("<")
                            .append(colorReplacement)
                            .append(">");
                    i++; // Skip the next character as it's part of the color code
                    continue;
                } else {
                    String formattingReplacement = FORMATTING_MAP.get(code);
                    if (formattingReplacement != null) {
                        result.append("<").append(formattingReplacement).append(">");
                        i++; // Skip the next character as it's part of the formatting code
                        continue;
                    }
                }
            }
            result.append(chars[i]);
        }

        return result.toString();
    }
}
