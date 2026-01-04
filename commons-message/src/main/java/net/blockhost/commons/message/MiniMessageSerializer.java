package net.blockhost.commons.message;

import de.exlll.configlib.Serializer;

/// ConfigLib serializer that converts legacy color codes to MiniMessage format.
///
/// Use this serializer with the {@code @SerializeWith} annotation to automatically
/// convert legacy color codes (both ampersand and section sign) when loading config values.
///
/// ## Example Usage
///
/// ```java
/// @Configuration
/// public class MyConfig {
///     @SerializeWith(serializer = MiniMessageSerializer.class)
///     public String welcomeMessage = "<green>Welcome!";
///
///     @SerializeWith(serializer = MiniMessageSerializer.class)
///     public String legacyMessage = "&aThis will be converted";
/// }
/// ```
///
/// When the config is loaded, any legacy color codes in the stored value will be
/// converted to MiniMessage format:
/// - {@code &a} becomes {@code <reset><green>}
/// - {@code §c} becomes {@code <reset><red>}
/// - {@code &#FF0000} becomes {@code <reset><#FF0000>}
/// - {@code &l} becomes {@code <bold>}
///
/// Values that are already in MiniMessage format are passed through unchanged.
///
/// @see MiniMessageConverter
/// @see de.exlll.configlib.SerializeWith
public final class MiniMessageSerializer implements Serializer<String, String> {

    /// Creates a new MiniMessage serializer.
    public MiniMessageSerializer() {}

    /// Serializes a MiniMessage string for storage.
    ///
    /// This method passes the value through unchanged, as MiniMessage
    /// is the target format for storage.
    ///
    /// @param element the MiniMessage string to serialize
    /// @return the same string unchanged
    @Override
    public String serialize(String element) {
        return element;
    }

    /// Deserializes a config value, converting legacy color codes to MiniMessage.
    ///
    /// This method uses {@link MiniMessageConverter} to convert any legacy
    /// color codes (ampersand or section sign format) to MiniMessage tags.
    ///
    /// @param element the string from the config file
    /// @return the string with legacy codes converted to MiniMessage format
    @Override
    public String deserialize(String element) {
        return MiniMessageConverter.convert(element);
    }
}
