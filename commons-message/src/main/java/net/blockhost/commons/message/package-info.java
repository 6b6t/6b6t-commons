/// Message utilities for 6b6t plugins.
///
/// This package provides utilities for handling MiniMessage-based message formatting
/// and sending. It simplifies working with Adventure's MiniMessage format and provides
/// a consistent API for plugin messaging.
///
/// ## ConfigLib Integration
///
/// Use {@link MiniMessageSerializer} to automatically convert legacy color codes
/// in your configuration files:
///
/// ```java
/// @Configuration
/// public class MyConfig {
///     @SerializeWith(serializer = MiniMessageSerializer.class)
///     public String message = "<green>Welcome!";
/// }
/// ```
///
/// ## Legacy Color Conversion
///
/// Use {@link MiniMessageConverter} to convert legacy color codes to MiniMessage:
///
/// ```java
/// String legacy = "&aHello &lWorld!";
/// String mini = MiniMessageConverter.convert(legacy);
/// // Result: "<reset><green>Hello <bold>World!"
/// ```
///
/// @see net.blockhost.commons.message.MessageService
/// @see net.blockhost.commons.message.MiniMessageConverter
/// @see net.blockhost.commons.message.MiniMessageSerializer
/// @see net.kyori.adventure.text.minimessage.MiniMessage
@NullMarked
package net.blockhost.commons.message;

import org.jspecify.annotations.NullMarked;
