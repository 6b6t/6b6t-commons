/// Message utilities for 6b6t plugins.
///
/// This package provides utilities for handling MiniMessage-based message formatting
/// and sending. It simplifies working with Adventure's MiniMessage format and provides
/// a consistent API for plugin messaging.
/// ## Quick Start
///
/// Create a message service backed by your configuration:
/// <pre>
/// `MessageService messages = MessageService.create(key ->plugin.getConfig().getString("messages." + key));`</pre>
///
/// Send messages to players:
/// <pre>
/// `// Simple messagemessages.send(player, "welcome");// Message with placeholdersmessages.send(player, "greeting", Map.of("player", player.getName()));// Using TagResolversmessages.send(player, "balance",Placeholder.parsed("amount", String.valueOf(balance)));`</pre>
///
/// Parse messages directly:
/// <pre>
/// `Component component = MessageService.parse("<green>Hello <player>!",Placeholder.parsed("player", playerName));`</pre>
///
/// @see net.blockhost.commons.message.MessageService
/// @see net.kyori.adventure.text.minimessage.MiniMessage
@NullMarked
package net.blockhost.commons.message;

import org.jspecify.annotations.NullMarked;
