# commons-command-bukkit

Bukkit/Paper command framework for 6b6t plugins, providing an easy-to-use subcommand system that integrates with Bukkit's command API.

## Installation

```kotlin
dependencies {
    implementation("net.blockhost.commons:commons-command-bukkit:1.0.0-SNAPSHOT")
}
```

## Features

- **Bukkit Integration**: Implements `CommandExecutor` and `TabCompleter`
- **Player-Only Commands**: Built-in filtering for player-only commands
- **Permission Support**: Uses Bukkit's permission system by default
- **Subcommand Routing**: Automatic routing to subcommand handlers
- **Tab Completion**: Full tab completion support

## Quick Start

### Define Subcommands

```java
public class HelpSubCommand implements BukkitSubCommand {
    
    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        player.sendMessage("Available commands: help, spawn, home");
    }
    
    @Override
    public @NotNull String getName() {
        return "help";
    }
    
    @Override
    public @NotNull List<String> getAliases() {
        return List.of("?");
    }
    
    @Override
    public @Nullable String getPermission() {
        return "myplugin.help";
    }
    
    @Override
    public @NotNull String getDescription() {
        return "Shows help information";
    }
}

public class SpawnSubCommand implements BukkitSubCommand {
    
    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        player.teleport(player.getWorld().getSpawnLocation());
        player.sendMessage("Teleported to spawn!");
    }
    
    @Override
    public @NotNull String getName() {
        return "spawn";
    }
    
    @Override
    public @Nullable String getPermission() {
        return "myplugin.spawn";
    }
}
```

### Create and Register Dispatcher

```java
public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        BukkitCommandDispatcher dispatcher = BukkitCommandDispatcher.builder()
            .defaultSubCommand("help")
            .register(new HelpSubCommand())
            .register(new SpawnSubCommand())
            .register(new HomeSubCommand())
            .unknownSubCommandHandler((player, args) -> {
                player.sendMessage(ChatColor.RED + "Unknown command: " + args[0]);
                player.sendMessage(ChatColor.YELLOW + "Use /mycommand help for usage");
            })
            .nonPlayerHandler((sender, args) -> {
                sender.sendMessage("This command can only be used by players!");
            })
            .permissionDeniedHandler((player, args) -> {
                player.sendMessage(ChatColor.RED + "You don't have permission!");
            })
            .build();
        
        PluginCommand command = getCommand("mycommand");
        command.setExecutor(dispatcher);
        command.setTabCompleter(dispatcher);
    }
}
```

### plugin.yml

```yaml
commands:
  mycommand:
    description: My plugin's main command
    aliases: [mc, myplugin]
    permission: myplugin.use
```

## Tab Completion Example

```java
public class TeleportSubCommand implements BukkitSubCommand {
    
    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 1) {
            player.sendMessage("Usage: /mycommand tp <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("Player not found!");
            return;
        }
        player.teleport(target);
        player.sendMessage("Teleported to " + target.getName());
    }
    
    @Override
    public @NotNull String getName() {
        return "tp";
    }
    
    @Override
    public @NotNull List<String> tabComplete(@NotNull Player player, @NotNull String[] args) {
        if (args.length == 1) {
            // Complete player names
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        }
        return List.of();
    }
    
    @Override
    public @Nullable String getPermission() {
        return "myplugin.teleport";
    }
}
```

## API Reference

### BukkitSubCommand

Convenience interface that extends `SubCommand<Player>`:

```java
public interface BukkitSubCommand extends SubCommand<Player> {
    // Inherits all methods from SubCommand<Player>
    // No additional methods - just provides type safety
}
```

### BukkitCommandDispatcher

| Method | Description |
|--------|-------------|
| `builder()` | Create a new dispatcher builder |
| `onCommand(...)` | Bukkit CommandExecutor implementation |
| `onTabComplete(...)` | Bukkit TabCompleter implementation |

### BukkitCommandDispatcher.Builder

| Method | Description |
|--------|-------------|
| `register(BukkitSubCommand)` | Register a subcommand |
| `defaultSubCommand(String)` | Command to run with no args |
| `unknownSubCommandHandler(BiConsumer<Player, String[]>)` | Handle unknown commands |
| `noArgsHandler(BiConsumer<Player, String[]>)` | Handle no arguments |
| `permissionDeniedHandler(BiConsumer<Player, String[]>)` | Handle permission denied |
| `nonPlayerHandler(BiConsumer<CommandSender, String[]>)` | Handle non-player senders |

## Command Flow

1. Command received by Bukkit
2. `BukkitCommandDispatcher.onCommand()` called
3. Check if sender is a Player (call `nonPlayerHandler` if not)
4. If no args and `defaultSubCommand` set, dispatch to it
5. If no args and no default, call `noArgsHandler`
6. Find subcommand by name/alias
7. If not found, call `unknownSubCommandHandler`
8. Check permission (call `permissionDeniedHandler` if denied)
9. Execute subcommand

## Permissions

By default, the dispatcher uses `Player::hasPermission`. Subcommands return their required permission from `getPermission()`:

```java
@Override
public @Nullable String getPermission() {
    return "myplugin.admin.reload";  // Required permission
    // return null;  // No permission required
}
```

## API Documentation

Full Javadoc available at: **https://6b6t.github.io/6b6t-commons/**

## Related Modules

- [commons-command-core](../commons-command-core) - Core command framework
- [commons-command-velocity](../commons-command-velocity) - Velocity implementation
- [commons-message](../commons-message) - Message formatting for responses
