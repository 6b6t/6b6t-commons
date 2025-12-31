# commons-command-core

Platform-independent command framework for 6b6t plugins, providing a flexible subcommand dispatcher system.

## Installation

```kotlin
dependencies {
    implementation("net.blockhost.commons:commons-command-core:1.0.0-SNAPSHOT")
}
```

> **Note**: For Bukkit/Paper plugins, use [commons-command-bukkit](../commons-command-bukkit).
> For Velocity plugins, use [commons-command-velocity](../commons-command-velocity).

## Features

- **Generic Type System**: Works with any command source type
- **Subcommand Routing**: Automatic routing to subcommand handlers
- **Tab Completion**: Built-in tab completion support
- **Permission Checking**: Pluggable permission system
- **Alias Support**: Multiple names for subcommands
- **Customizable Handlers**: Default command, unknown command, no args, permission denied

## Quick Start

### Define a Subcommand

```java
public class HelpSubCommand implements SubCommand<CommandSender> {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("Available commands: help, info, reload");
    }
    
    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("?", "h");
    }
    
    @Override
    public String getDescription() {
        return "Shows help information";
    }
    
    @Override
    public String getUsage() {
        return "/mycommand help";
    }
    
    @Override
    public String getPermission() {
        return "myplugin.help";
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();  // No tab completions
    }
}
```

### Create a Dispatcher

```java
CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher.Builder<CommandSender>()
    .defaultSubCommand("help")
    .register(new HelpSubCommand())
    .register(new InfoSubCommand())
    .register(new ReloadSubCommand())
    .unknownSubCommandHandler((sender, args) -> {
        sender.sendMessage("Unknown command: " + args[0]);
    })
    .noArgsHandler((sender, args) -> {
        sender.sendMessage("Use /mycommand help for usage");
    })
    .permissionDeniedHandler((sender, args) -> {
        sender.sendMessage("You don't have permission!");
    })
    .permissionChecker((sender, permission) -> sender.hasPermission(permission))
    .build();
```

### Use the Dispatcher

```java
// Dispatch a command
dispatcher.dispatch(sender, new String[]{"help"});

// Get tab completions
List<String> completions = dispatcher.tabComplete(sender, new String[]{"he"});
// Returns: ["help"]

// Find a subcommand
Optional<SubCommand<CommandSender>> cmd = dispatcher.findSubCommand("help");
Optional<SubCommand<CommandSender>> alias = dispatcher.findSubCommand("?");  // Same command

// Get all subcommands
Collection<SubCommand<CommandSender>> all = dispatcher.getSubCommands();
```

## API Reference

### SubCommand<S>

The interface for implementing subcommands:

| Method | Required | Description |
|--------|----------|-------------|
| `execute(S, String[])` | Yes | Execute the command |
| `getName()` | Yes | Primary command name |
| `getAliases()` | No | Alternative names (default: empty) |
| `tabComplete(S, String[])` | No | Tab completions (default: empty) |
| `getPermission()` | No | Required permission (default: null) |
| `getDescription()` | No | Command description |
| `getUsage()` | No | Usage string |

### CommandDispatcher<S>

Routes commands to subcommand handlers:

| Method | Description |
|--------|-------------|
| `dispatch(S, String[])` | Execute a command |
| `tabComplete(S, String[])` | Get tab completions |
| `findSubCommand(String)` | Find subcommand by name/alias |
| `getSubCommands()` | Get all registered subcommands |

### CommandDispatcher.Builder<S>

| Method | Description |
|--------|-------------|
| `register(SubCommand<S>)` | Register a subcommand |
| `defaultSubCommand(String)` | Command to run with no args |
| `unknownSubCommandHandler(BiConsumer)` | Handle unknown commands |
| `noArgsHandler(BiConsumer)` | Handle no arguments |
| `permissionDeniedHandler(BiConsumer)` | Handle permission denied |
| `permissionChecker(BiPredicate)` | Custom permission checking |

## Subcommand Matching

- Matching is **case-insensitive**
- Both primary name and aliases are checked
- First matching subcommand is executed

```java
// All these work for a command with name "help" and alias "?"
dispatcher.dispatch(sender, new String[]{"help"});
dispatcher.dispatch(sender, new String[]{"HELP"});
dispatcher.dispatch(sender, new String[]{"?"});
```

## Tab Completion

Tab completion follows this logic:

1. If no args or completing first arg: suggest matching subcommand names
2. If subcommand identified: delegate to subcommand's `tabComplete()`

```java
dispatcher.tabComplete(sender, new String[]{""});      // All commands
dispatcher.tabComplete(sender, new String[]{"he"});    // Commands starting with "he"
dispatcher.tabComplete(sender, new String[]{"help", ""}); // Subcommand's completions
```

## API Documentation

Full Javadoc available at: **https://6b6t.github.io/6b6t-commons/**

## Related Modules

- [commons-command-bukkit](../commons-command-bukkit) - Bukkit/Paper implementation
- [commons-command-velocity](../commons-command-velocity) - Velocity implementation
