/**
 * Message utilities for 6b6t plugins.
 *
 * <p>This package provides utilities for handling MiniMessage-based message formatting
 * and sending. It simplifies working with Adventure's MiniMessage format and provides
 * a consistent API for plugin messaging.
 *
 * <h2>Quick Start</h2>
 *
 * <p>Create a message service backed by your configuration:
 * <pre>{@code
 * MessageService messages = MessageService.create(key ->
 *     plugin.getConfig().getString("messages." + key)
 * );
 * }</pre>
 *
 * <p>Send messages to players:
 * <pre>{@code
 * // Simple message
 * messages.send(player, "welcome");
 *
 * // Message with placeholders
 * messages.send(player, "greeting", Map.of("player", player.getName()));
 *
 * // Using TagResolvers
 * messages.send(player, "balance",
 *     Placeholder.parsed("amount", String.valueOf(balance))
 * );
 * }</pre>
 *
 * <p>Parse messages directly:
 * <pre>{@code
 * Component component = MessageService.parse("<green>Hello <player>!",
 *     Placeholder.parsed("player", playerName)
 * );
 * }</pre>
 *
 * @see net.blockhost.commons.message.MessageService
 * @see net.kyori.adventure.text.minimessage.MiniMessage
 */
@NullMarked
package net.blockhost.commons.message;

import org.jspecify.annotations.NullMarked;
