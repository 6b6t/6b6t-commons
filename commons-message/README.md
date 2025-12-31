# commons-message

MiniMessage formatting and messaging utilities for 6b6t plugins, built on [Adventure](https://docs.advntr.dev/).

## Installation

```kotlin
dependencies {
    implementation("net.blockhost.commons:commons-message:1.0.0-SNAPSHOT")
}
```

## Features

- **MiniMessage Parsing**: Parse MiniMessage format strings to Adventure Components
- **Message Provider Pattern**: Abstract message lookup for i18n/configuration support
- **Placeholder Support**: Easy placeholder replacement with type-safe API
- **Audience Integration**: Send messages to any Adventure Audience (players, console, etc.)

## Quick Start

### Static Parsing

```java
// Simple parsing
Component message = MessageService.parse("<green>Hello <bold>World</bold>!");

// With placeholders
Component welcome = MessageService.parse(
    "<yellow>Welcome, <player>!",
    MessageService.placeholder("player", playerName)
);

// With placeholder map
Component balance = MessageService.parse(
    "<gold>Balance: <white><amount>",
    Map.of("amount", "1,000")
);
```

### Message Service with Provider

```java
// Create service with message lookup function
MessageService messages = MessageService.create(key -> config.getMessage(key));

// Get parsed messages
Component welcome = messages.get("welcome");
Component balance = messages.get("balance", Map.of("amount", "1000"));

// Send directly to players
messages.send(player, "welcome");
messages.send(player, "balance", Map.of("amount", "1000"));

// Send raw MiniMessage strings
messages.sendRaw(player, "<red>Custom message here!");
```

### With Configuration

```java
@Configuration
public class MessagesConfig {
    private String welcome = "<green>Welcome to the server!";
    private String balance = "<gold>Your balance: <white><amount>";
    private String noPermission = "<red>You don't have permission!";
    
    public String getMessage(String key) {
        return switch (key) {
            case "welcome" -> welcome;
            case "balance" -> balance;
            case "noPermission" -> noPermission;
            default -> null;
        };
    }
}

// Setup
MessagesConfig config = ConfigLoader.loadOrCreate(path, MessagesConfig.class);
MessageService messages = MessageService.create(config::getMessage);

// Usage
messages.send(player, "welcome");
```

## MiniMessage Format

This library uses [MiniMessage](https://docs.advntr.dev/minimessage/) format:

### Basic Tags

```
<red>Red text</red>
<green>Green text
<bold>Bold text</bold>
<italic>Italic text
<underlined>Underlined
<strikethrough>Strikethrough
<obfuscated>Obfuscated
```

### Colors

```
<#ff5555>Hex color
<color:red>Named color
<gradient:red:blue>Gradient text</gradient>
<rainbow>Rainbow text</rainbow>
```

### Events

```
<click:run_command:/help>Click to run /help</click>
<click:suggest_command:/msg >Click to suggest</click>
<click:open_url:https://example.com>Click to open URL</click>
<hover:show_text:'Hover text'>Hover over me</hover>
```

### Placeholders

```java
// Using TagResolver
Component msg = MessageService.parse(
    "Hello <player>!",
    MessageService.placeholder("player", "Steve")
);

// Multiple placeholders
Component msg = MessageService.parse(
    "<player> has <coins> coins",
    MessageService.resolvers(
        MessageService.placeholder("player", playerName),
        MessageService.placeholder("coins", String.valueOf(coins))
    )
);
```

## API Reference

### MessageService

| Method | Description |
|--------|-------------|
| `create(Function<String, String>)` | Create service with message provider |
| `parse(String)` | Parse MiniMessage to Component |
| `parse(String, TagResolver...)` | Parse with tag resolvers |
| `parse(String, Map<String, String>)` | Parse with placeholder map |
| `get(String)` | Get message by key |
| `get(String, Map<String, String>)` | Get message with placeholders |
| `send(Audience, String)` | Send message to audience |
| `send(Audience, String, Map<String, String>)` | Send with placeholders |
| `sendRaw(Audience, String)` | Send raw MiniMessage string |
| `placeholder(String, String)` | Create placeholder resolver |
| `resolvers(TagResolver...)` | Combine multiple resolvers |
| `miniMessage()` | Get shared MiniMessage instance |

### Missing Messages

When a message key is not found, the service returns a helpful placeholder:

```java
Component msg = messages.get("nonexistent");
// Returns: <Missing message: nonexistent>
```

## API Documentation

Full Javadoc available at: **https://6b6t.github.io/6b6t-commons/**

## Related Modules

- [commons-config](../commons-config) - For loading message configurations
- [commons-command-bukkit](../commons-command-bukkit) - Command framework with messaging
