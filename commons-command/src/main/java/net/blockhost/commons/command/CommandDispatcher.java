package net.blockhost.commons.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/// Dispatcher for handling commands with multiple subcommands.
///
/// This class implements both [CommandExecutor] and [TabCompleter]
/// to provide a complete command handling solution. It routes incoming commands
/// to the appropriate [SubCommand] based on the first argument.
///
/// Example usage:
/// <pre>
/// `CommandDispatcher dispatcher =
// CommandDispatcher.builder().defaultSubCommand("help").playerOnlyMessage(Component.text("This command is for players
// only!", NamedTextColor.RED)).unknownSubCommandHandler((player, args) ->{player.sendMessage(Component.text("Unknown
// command: " + args[0]));}).register(new HelpSubCommand()).register(new TeleportSubCommand()).register(new
// SettingsSubCommand()).build();// Register with
// Bukkitplugin.getCommand("mycommand").setExecutor(dispatcher);plugin.getCommand("mycommand").setTabCompleter(dispatcher);`</pre>
///
/// @see SubCommand
public final class CommandDispatcher implements CommandExecutor, TabCompleter {

    private final List<SubCommand> subCommands;
    private final String defaultSubCommand;
    private final BiConsumer<Player, String[]> unknownSubCommandHandler;
    private final BiConsumer<CommandSender, String[]> nonPlayerHandler;

    private CommandDispatcher(Builder builder) {
        this.subCommands = new ArrayList<>(builder.subCommands);
        this.defaultSubCommand = builder.defaultSubCommand;
        this.unknownSubCommandHandler = builder.unknownSubCommandHandler;
        this.nonPlayerHandler = builder.nonPlayerHandler;
    }

    /// Creates a new builder for CommandDispatcher.
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

        if (args.length == 0) {
            if (defaultSubCommand != null) {
                Optional<SubCommand> sub = findSubCommand(defaultSubCommand);
                sub.ifPresent(sc -> sc.execute(player, args));
            }
            return true;
        }

        Optional<SubCommand> subCommand = findSubCommand(args[0]);
        if (subCommand.isPresent()) {
            SubCommand sc = subCommand.get();
            String permission = sc.getPermission();
            if (permission != null && !player.hasPermission(permission)) {
                // Permission denied - could add a handler for this
                return true;
            }
            sc.execute(player, args);
        } else if (unknownSubCommandHandler != null) {
            unknownSubCommandHandler.accept(player, args);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            for (SubCommand sc : subCommands) {
                if (sc.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                    completions.add(sc.getName());
                }
                for (String alias : sc.getAliases()) {
                    if (alias.toLowerCase(Locale.ROOT).startsWith(input)) {
                        completions.add(alias);
                    }
                }
            }
        } else if (args.length > 1) {
            Optional<SubCommand> subCommand = findSubCommand(args[0]);
            subCommand.ifPresent(sc -> completions.addAll(sc.tabComplete(player, args)));
        }

        return completions;
    }

    /// Finds a subcommand by name or alias.
    ///
    /// @param name the name or alias to search for
    /// @return an Optional containing the subcommand if found
    public @NotNull Optional<SubCommand> findSubCommand(@NotNull String name) {
        for (SubCommand sc : subCommands) {
            if (sc.getName().equalsIgnoreCase(name)) {
                return Optional.of(sc);
            }
            for (String alias : sc.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return Optional.of(sc);
                }
            }
        }
        return Optional.empty();
    }

    /// Returns an unmodifiable list of registered subcommands.
    ///
    /// @return the list of subcommands
    public @NotNull List<SubCommand> getSubCommands() {
        return List.copyOf(subCommands);
    }

    /// Builder for creating [CommandDispatcher] instances.
    public static final class Builder {

        private final List<SubCommand> subCommands = new ArrayList<>();
        private String defaultSubCommand;
        private BiConsumer<Player, String[]> unknownSubCommandHandler;
        private BiConsumer<CommandSender, String[]> nonPlayerHandler;

        private Builder() {}

        /// Registers a subcommand with the dispatcher.
        ///
        /// @param subCommand the subcommand to register
        /// @return this builder
        public @NotNull Builder register(@NotNull SubCommand subCommand) {
            this.subCommands.add(Objects.requireNonNull(subCommand, "subCommand"));
            return this;
        }

        /// Registers multiple subcommands with the dispatcher.
        ///
        /// @param subCommands the subcommands to register
        /// @return this builder
        public @NotNull Builder registerAll(@NotNull SubCommand... subCommands) {
            for (SubCommand sc : subCommands) {
                register(sc);
            }
            return this;
        }

        /// Sets the default subcommand to execute when no arguments are provided.
        ///
        /// @param subCommandName the name of the default subcommand
        /// @return this builder
        public @NotNull Builder defaultSubCommand(@Nullable String subCommandName) {
            this.defaultSubCommand = subCommandName;
            return this;
        }

        /// Sets the handler for unknown subcommands.
        ///
        /// @param handler the handler to invoke when an unknown subcommand is used
        /// @return this builder
        public @NotNull Builder unknownSubCommandHandler(@Nullable BiConsumer<Player, String[]> handler) {
            this.unknownSubCommandHandler = handler;
            return this;
        }

        /// Sets the handler for non-player command senders.
        ///
        /// @param handler the handler to invoke when a non-player uses the command
        /// @return this builder
        public @NotNull Builder nonPlayerHandler(@Nullable BiConsumer<CommandSender, String[]> handler) {
            this.nonPlayerHandler = handler;
            return this;
        }

        /// Builds the CommandDispatcher.
        ///
        /// @return a new CommandDispatcher instance
        public @NotNull CommandDispatcher build() {
            return new CommandDispatcher(this);
        }
    }
}
