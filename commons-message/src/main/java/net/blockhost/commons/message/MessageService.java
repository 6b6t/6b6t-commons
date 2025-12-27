package net.blockhost.commons.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Service for handling MiniMessage-based message formatting and sending.
 *
 * <p>This service provides a convenient API for deserializing MiniMessage strings,
 * applying placeholders, and sending messages to audiences (players, console, etc.).
 *
 * <p>Example usage:
 * <pre>{@code
 * // Create a message service with a message provider
 * MessageService messages = MessageService.create(key -> config.getString("messages." + key));
 *
 * // Send a simple message
 * messages.send(player, "welcome");
 *
 * // Send a message with placeholders
 * messages.send(player, "player-joined", Map.of("player", playerName));
 *
 * // Or use TagResolvers directly
 * messages.send(player, "balance", Placeholder.parsed("amount", balance));
 * }</pre>
 *
 * @see MiniMessage
 * @see TagResolver
 */
public final class MessageService {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Function<String, String> messageProvider;

    private MessageService(@NotNull Function<String, String> messageProvider) {
        this.messageProvider = Objects.requireNonNull(messageProvider, "messageProvider");
    }

    /**
     * Creates a new message service with the specified message provider.
     *
     * <p>The message provider is a function that takes a message key and returns
     * the raw MiniMessage string for that key. This allows integration with any
     * configuration system.
     *
     * @param messageProvider function that retrieves raw messages by key
     * @return a new message service
     */
    public static @NotNull MessageService create(@NotNull Function<String, String> messageProvider) {
        return new MessageService(messageProvider);
    }

    /**
     * Parses a MiniMessage string into a Component.
     *
     * @param message the MiniMessage string
     * @return the parsed component
     */
    public static @NotNull Component parse(@NotNull String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Parses a MiniMessage string with tag resolvers.
     *
     * @param message   the MiniMessage string
     * @param resolvers the tag resolvers to apply
     * @return the parsed component
     */
    public static @NotNull Component parse(@NotNull String message, @NotNull TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(message, resolvers);
    }

    /**
     * Parses a MiniMessage string with placeholder replacements.
     *
     * <p>Placeholders should be referenced in the message using angle brackets,
     * e.g., {@code <player>} for a placeholder with key "player".
     *
     * @param message      the MiniMessage string
     * @param placeholders map of placeholder keys to values
     * @return the parsed component
     */
    public static @NotNull Component parse(@NotNull String message, @NotNull Map<String, String> placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        placeholders.forEach((key, value) -> builder.resolver(Placeholder.parsed(key, value)));
        return MINI_MESSAGE.deserialize(message, builder.build());
    }

    /**
     * Gets a message by key and parses it.
     *
     * @param key the message key
     * @return the parsed component, or an error component if the message is not found
     */
    public @NotNull Component get(@NotNull String key) {
        String raw = messageProvider.apply(key);
        if (raw == null) {
            return Component.text("<Missing message: " + key + ">");
        }
        return MINI_MESSAGE.deserialize(raw);
    }

    /**
     * Gets a message by key and parses it with tag resolvers.
     *
     * @param key       the message key
     * @param resolvers the tag resolvers to apply
     * @return the parsed component
     */
    public @NotNull Component get(@NotNull String key, @NotNull TagResolver... resolvers) {
        String raw = messageProvider.apply(key);
        if (raw == null) {
            return Component.text("<Missing message: " + key + ">");
        }
        return MINI_MESSAGE.deserialize(raw, resolvers);
    }

    /**
     * Gets a message by key and parses it with placeholder replacements.
     *
     * @param key          the message key
     * @param placeholders map of placeholder keys to values
     * @return the parsed component
     */
    public @NotNull Component get(@NotNull String key, @NotNull Map<String, String> placeholders) {
        String raw = messageProvider.apply(key);
        if (raw == null) {
            return Component.text("<Missing message: " + key + ">");
        }
        return parse(raw, placeholders);
    }

    /**
     * Sends a message to an audience.
     *
     * @param audience the audience to send to
     * @param key      the message key
     */
    public void send(@NotNull Audience audience, @NotNull String key) {
        audience.sendMessage(get(key));
    }

    /**
     * Sends a message to an audience with tag resolvers.
     *
     * @param audience  the audience to send to
     * @param key       the message key
     * @param resolvers the tag resolvers to apply
     */
    public void send(@NotNull Audience audience, @NotNull String key, @NotNull TagResolver... resolvers) {
        audience.sendMessage(get(key, resolvers));
    }

    /**
     * Sends a message to an audience with placeholder replacements.
     *
     * @param audience     the audience to send to
     * @param key          the message key
     * @param placeholders map of placeholder keys to values
     */
    public void send(@NotNull Audience audience, @NotNull String key, @NotNull Map<String, String> placeholders) {
        audience.sendMessage(get(key, placeholders));
    }

    /**
     * Sends a raw MiniMessage string to an audience.
     *
     * @param audience the audience to send to
     * @param message  the MiniMessage string
     */
    public void sendRaw(@NotNull Audience audience, @NotNull String message) {
        audience.sendMessage(parse(message));
    }

    /**
     * Sends a raw MiniMessage string to an audience with tag resolvers.
     *
     * @param audience  the audience to send to
     * @param message   the MiniMessage string
     * @param resolvers the tag resolvers to apply
     */
    public void sendRaw(@NotNull Audience audience, @NotNull String message, @NotNull TagResolver... resolvers) {
        audience.sendMessage(parse(message, resolvers));
    }

    /**
     * Creates a placeholder resolver for a key-value pair.
     *
     * @param key   the placeholder key
     * @param value the placeholder value
     * @return a tag resolver for the placeholder
     */
    public static @NotNull TagResolver placeholder(@NotNull String key, @NotNull String value) {
        return Placeholder.parsed(key, value);
    }

    /**
     * Creates a placeholder resolver for a key-component pair.
     *
     * @param key       the placeholder key
     * @param component the component value
     * @return a tag resolver for the placeholder
     */
    public static @NotNull TagResolver placeholder(@NotNull String key, @NotNull Component component) {
        return Placeholder.component(key, component);
    }

    /**
     * Combines multiple tag resolvers into one.
     *
     * @param resolvers the resolvers to combine
     * @return a combined tag resolver
     */
    public static @NotNull TagResolver resolvers(@NotNull TagResolver... resolvers) {
        return TagResolver.resolver(resolvers);
    }

    /**
     * Returns the shared MiniMessage instance.
     *
     * @return the MiniMessage instance
     */
    public static @NotNull MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }
}
