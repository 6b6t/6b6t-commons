# commons-command-velocity

Velocity command framework for 6b6t plugins, providing an easy-to-use subcommand system that integrates with Velocity's command API.

## Installation

```kotlin
dependencies {
    implementation("net.blockhost.commons:commons-command-velocity:1.0.0-SNAPSHOT")
}
```

## Features

- **Velocity Integration**: Implements `SimpleCommand` interface
- **Async Tab Completion**: Returns `CompletableFuture<List<String>>`
- **Player-Only Mode**: Optional restriction to player senders only
- **Permission Support**: Uses Velocity's permission system by default
- **Subcommand Routing**: Automatic routing to subcommand handlers

## Quick Start

### Define Subcommands

```java
public class ServerSubCommand implements VelocitySubCommand {
    
    private final ProxyServer server;
    
    public ServerSubCommand(ProxyServer server) {
        this.server = server;
    }
    
    @Override
    public void execute(@NotNull CommandSource source, @NotNull String[] args) {
        if (args.length < 1) {
            source.sendMessage(Component.text("Usage: /proxy server <name>"));
            return;
        }
        
        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("Only players can switch servers!"));
            return;
        }
        
        server.getServer(args[0]).ifPresentOrElse(
            s -> player.createConnectionRequest(s).fireAndForget(),
            () -> source.sendMessage(Component.text("Server not found!"))
        );
    }
    
    @Override
    public @NotNull String getName() {
        return "server";
    }
    
    @Override
    public @NotNull List<String> getAliases() {
        return List.of("s", "switch");
    }
    
    @Override
    public @Nullable String getPermission() {
        return "proxy.server";
    }
    
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSource source, @NotNull String[] args) {
        if (args.length == 1) {
            return server.getAllServers().stream()
                .map(s -> s.getServerInfo().getName())
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        }
        return List.of();
    }
}
```

### Create and Register Dispatcher

```java
@Plugin(id = "myproxy", name = "MyProxy", version = "1.0.0")
public class MyProxyPlugin {
    
    private final ProxyServer server;
    private final CommandManager commandManager;
    
    @Inject
    public MyProxyPlugin(ProxyServer server, CommandManager commandManager) {
        this.server = server;
        this.commandManager = commandManager;
    }
    
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        VelocityCommandDispatcher dispatcher = VelocityCommandDispatcher.builder()
            .defaultSubCommand("help")
            .register(new HelpSubCommand())
            .register(new ServerSubCommand(server))
            .register(new ListSubCommand(server))
            .unknownSubCommandHandler((source, args) -> {
                source.sendMessage(Component.text("Unknown command: " + args[0])
                    .color(NamedTextColor.RED));
            })
            .permissionDeniedHandler((source, args) -> {
                source.sendMessage(Component.text("You don't have permission!")
                    .color(NamedTextColor.RED));
            })
            .build();
        
        commandManager.register("proxy", dispatcher);
    }
}
```

## Player-Only Commands

For commands that should only be used by players:

```java
VelocityCommandDispatcher dispatcher = VelocityCommandDispatcher.builder()
    .playerOnly(true)
    .nonPlayerHandler((source, args) -> {
        source.sendMessage(Component.text("This command can only be used by players!"));
    })
    .register(new LobbySubCommand())
    .build();
```

## API Reference

### VelocitySubCommand

Convenience interface that extends `SubCommand<CommandSource>`:

```java
public interface VelocitySubCommand extends SubCommand<CommandSource> {
    // Inherits all methods from SubCommand<CommandSource>
    // Works with any CommandSource (Player, ConsoleCommandSource, etc.)
}
```

### VelocityCommandDispatcher

| Method | Description |
|--------|-------------|
| `builder()` | Create a new dispatcher builder |
| `execute(Invocation)` | Velocity SimpleCommand implementation |
| `suggestAsync(Invocation)` | Async tab completion |

### VelocityCommandDispatcher.Builder

| Method | Description |
|--------|-------------|
| `register(VelocitySubCommand)` | Register a subcommand |
| `defaultSubCommand(String)` | Command to run with no args |
| `playerOnly(boolean)` | Restrict to players only |
| `unknownSubCommandHandler(BiConsumer<CommandSource, String[]>)` | Handle unknown commands |
| `noArgsHandler(BiConsumer<CommandSource, String[]>)` | Handle no arguments |
| `permissionDeniedHandler(BiConsumer<CommandSource, String[]>)` | Handle permission denied |
| `nonPlayerHandler(BiConsumer<CommandSource, String[]>)` | Handle non-player senders |

## Tab Completion

Tab completion in Velocity is asynchronous. The dispatcher automatically wraps your subcommand's `tabComplete()` method:

```java
@Override
public @NotNull List<String> tabComplete(@NotNull CommandSource source, @NotNull String[] args) {
    if (args.length == 1) {
        // Return server names matching the input
        return proxy.getAllServers().stream()
            .map(s -> s.getServerInfo().getName())
            .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
            .toList();
    }
    return List.of();
}
```

The dispatcher calls `CompletableFuture.completedFuture()` with your result.

## Command Source Types

Velocity commands can come from different sources:

```java
@Override
public void execute(@NotNull CommandSource source, @NotNull String[] args) {
    if (source instanceof Player player) {
        // Player-specific logic
        player.sendMessage(Component.text("Hello, " + player.getUsername()));
    } else if (source instanceof ConsoleCommandSource) {
        // Console-specific logic
        source.sendMessage(Component.text("Hello from console!"));
    }
}
```

## Permissions

By default, the dispatcher uses `CommandSource::hasPermission`:

```java
@Override
public @Nullable String getPermission() {
    return "myproxy.admin.reload";  // Required permission
    // return null;  // No permission required
}
```

## Complete Plugin Example

```java
@Plugin(id = "serverutils", name = "ServerUtils", version = "1.0.0")
public class ServerUtilsPlugin {
    
    @Inject
    public ServerUtilsPlugin(ProxyServer server, CommandManager commandManager) {
        VelocityCommandDispatcher dispatcher = VelocityCommandDispatcher.builder()
            .defaultSubCommand("help")
            .register(new VelocitySubCommand() {
                @Override
                public void execute(@NotNull CommandSource source, @NotNull String[] args) {
                    source.sendMessage(Component.text("Commands: help, list, info"));
                }
                
                @Override
                public @NotNull String getName() { return "help"; }
            })
            .register(new VelocitySubCommand() {
                @Override
                public void execute(@NotNull CommandSource source, @NotNull String[] args) {
                    int count = server.getPlayerCount();
                    source.sendMessage(Component.text("Online: " + count + " players"));
                }
                
                @Override
                public @NotNull String getName() { return "list"; }
                
                @Override
                public @Nullable String getPermission() { return "serverutils.list"; }
            })
            .unknownSubCommandHandler((source, args) -> 
                source.sendMessage(Component.text("Unknown: " + args[0]).color(NamedTextColor.RED)))
            .build();
        
        commandManager.register("su", dispatcher);
    }
}
```

## API Documentation

Full Javadoc available at: **https://6b6t.github.io/6b6t-commons/**

## Related Modules

- [commons-command-core](../commons-command-core) - Core command framework
- [commons-command-bukkit](../commons-command-bukkit) - Bukkit implementation
- [commons-message](../commons-message) - Message formatting for responses
