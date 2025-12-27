package net.blockhost.commons.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.function.Function;

/// Service for handling MiniMessage-based message formatting and sending.
///
/// This service provides a convenient API for deserializing MiniMessage strings,
/// applying placeholders, and sending messages to audiences (players, console, etc.).
///
/// Example usage:
/// ```java
/// // Create a message service with a message provider
/// MessageService messages = MessageService.create(key -> config.getString("messages." + key));
///
/// // Send a simple message
/// messages.send(player, "welcome");
///
/// // Send a message with placeholders
/// messages.send(player, "player-joined", Map.of("player", playerName));
///
/// // Or use TagResolvers directly
/// messages.send(player, "balance", Placeholder.parsed("amount", balance));
/// ```
///
/// @see MiniMessage
/// @see TagResolver
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageService {

    @Getter
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Function<String, String> messageProvider;

    /// Creates a new message service with the specified message provider.
    ///
    /// The message provider is a function that takes a message key and returns
    /// the raw MiniMessage string for that key. This allows integration with any
    /// configuration system.
    ///
    /// @param messageProvider function that retrieves raw messages by key
    /// @return a new message service
    public static MessageService create(Function<String, String> messageProvider) {
        return new MessageService(messageProvider);
    }

    /// Parses a MiniMessage string into a Component.
    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /// Parses a MiniMessage string with tag resolvers.
    public static Component parse(String message, TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(message, resolvers);
    }

    /// Parses a MiniMessage string with placeholder replacements.
    public static Component parse(String message, Map<String, String> placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        placeholders.forEach((key, value) -> builder.resolver(Placeholder.parsed(key, value)));
        return MINI_MESSAGE.deserialize(message, builder.build());
    }

    /// Gets a message by key and parses it.
    public Component get(String key) {
        String raw = messageProvider.apply(key);
        if (raw == null) {
            return Component.text("<Missing message: " + key + ">");
        }
        return MINI_MESSAGE.deserialize(raw);
    }

    /// Gets a message by key and parses it with tag resolvers.
    public Component get(String key, TagResolver... resolvers) {
        String raw = messageProvider.apply(key);
        if (raw == null) {
            return Component.text("<Missing message: " + key + ">");
        }
        return MINI_MESSAGE.deserialize(raw, resolvers);
    }

    /// Gets a message by key and parses it with placeholder replacements.
    public Component get(String key, Map<String, String> placeholders) {
        String raw = messageProvider.apply(key);
        if (raw == null) {
            return Component.text("<Missing message: " + key + ">");
        }
        return parse(raw, placeholders);
    }

    /// Sends a message to an audience.
    public void send(Audience audience, String key) {
        audience.sendMessage(get(key));
    }

    /// Sends a message to an audience with tag resolvers.
    public void send(Audience audience, String key, TagResolver... resolvers) {
        audience.sendMessage(get(key, resolvers));
    }

    /// Sends a message to an audience with placeholder replacements.
    public void send(Audience audience, String key, Map<String, String> placeholders) {
        audience.sendMessage(get(key, placeholders));
    }

    /// Sends a raw MiniMessage string to an audience.
    public void sendRaw(Audience audience, String message) {
        audience.sendMessage(parse(message));
    }

    /// Sends a raw MiniMessage string to an audience with tag resolvers.
    public void sendRaw(Audience audience, String message, TagResolver... resolvers) {
        audience.sendMessage(parse(message, resolvers));
    }

    /// Creates a placeholder resolver for a key-value pair.
    public static TagResolver placeholder(String key, String value) {
        return Placeholder.parsed(key, value);
    }

    /// Creates a placeholder resolver for a key-component pair.
    public static TagResolver placeholder(String key, Component component) {
        return Placeholder.component(key, component);
    }

    /// Combines multiple tag resolvers into one.
    public static TagResolver resolvers(TagResolver... resolvers) {
        return TagResolver.resolver(resolvers);
    }

    /// Returns the shared MiniMessage instance.
    public static MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }
}
