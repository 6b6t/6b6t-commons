package net.blockhost.commons.command.bukkit;

import net.blockhost.commons.command.CommandDispatcher;
import net.blockhost.commons.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

/// Bukkit-specific command dispatcher that implements [CommandExecutor] and [TabCompleter].
///
/// This class integrates the generic [CommandDispatcher] with Bukkit's command system,
/// handling player-only commands with permission checking.
///
/// Example usage:
/// ```java
/// BukkitCommandDispatcher dispatcher = BukkitCommandDispatcher.builder()
///     .defaultSubCommand("help")
///     .playerOnlyMessage(player -> player.sendMessage("This command is for players only!"))
///     .unknownSubCommandHandler((player, args) -> {
///         player.sendMessage("Unknown command: " + args[0]);
///     })
///     .register(new HelpSubCommand())
///     .register(new TeleportSubCommand())
///     .build();
///
/// // Register with Bukkit
/// plugin.getCommand("mycommand").setExecutor(dispatcher);
/// plugin.getCommand("mycommand").setTabCompleter(dispatcher);
/// ```
///
/// @see BukkitSubCommand
/// @see CommandDispatcher
public final class BukkitCommandDispatcher extends CommandDispatcher<Player> implements CommandExecutor, TabCompleter {

    private final BiConsumer<CommandSender, String[]> nonPlayerHandler;

    private BukkitCommandDispatcher(Builder builder) {
        super(builder);
        this.nonPlayerHandler = builder.nonPlayerHandler;
    }

    /// Creates a new builder for BukkitCommandDispatcher.
    ///
    /// @return a new builder instance
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            if (nonPlayerHandler != null) {
                nonPlayerHandler.accept(sender, args);
            }
            return true;
        }

        return dispatch(player, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        return tabComplete(player, args);
    }

    /// Builder for creating [BukkitCommandDispatcher] instances.
    public static final class Builder extends CommandDispatcher.Builder<Player> {

        private BiConsumer<CommandSender, String[]> nonPlayerHandler;

        private Builder() {
            // Set up default Bukkit permission checker
            permissionChecker(Player::hasPermission);
        }

        /// Sets the handler for non-player command senders.
        ///
        /// @param handler the handler to invoke when a non-player uses the command
        /// @return this builder
        public @NotNull Builder nonPlayerHandler(@Nullable BiConsumer<CommandSender, String[]> handler) {
            this.nonPlayerHandler = handler;
            return this;
        }

        @Override
        public @NotNull Builder register(@NotNull SubCommand<Player> subCommand) {
            super.register(subCommand);
            return this;
        }

        @Override
        public @NotNull Builder defaultSubCommand(@Nullable String subCommandName) {
            super.defaultSubCommand(subCommandName);
            return this;
        }

        @Override
        public @NotNull Builder unknownSubCommandHandler(@Nullable BiConsumer<Player, String[]> handler) {
            super.unknownSubCommandHandler(handler);
            return this;
        }

        @Override
        public @NotNull Builder noArgsHandler(@Nullable BiConsumer<Player, String[]> handler) {
            super.noArgsHandler(handler);
            return this;
        }

        @Override
        public @NotNull Builder permissionDeniedHandler(@Nullable BiConsumer<Player, SubCommand<Player>> handler) {
            super.permissionDeniedHandler(handler);
            return this;
        }

        @Override
        public @NotNull BukkitCommandDispatcher build() {
            return new BukkitCommandDispatcher(this);
        }
    }
}
