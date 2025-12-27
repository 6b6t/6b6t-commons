package net.blockhost.commons.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/// Generic dispatcher for handling commands with multiple subcommands.
///
/// This class routes incoming commands to the appropriate [SubCommand] based on
/// the first argument. Platform-specific implementations should extend or wrap
/// this class to integrate with their command systems.
///
/// The type parameter `S` represents the command source type, which varies by platform:
///
/// - For Bukkit/Paper: `Player` or `CommandSender`
/// - For Velocity: `CommandSource`
///
/// Example usage:
/// ```java
/// CommandDispatcher<Player> dispatcher = new CommandDispatcher.Builder<Player>()
///     .defaultSubCommand("help")
///     .permissionChecker((player, perm) -> player.hasPermission(perm))
///     .unknownSubCommandHandler((player, args) -> {
///         player.sendMessage("Unknown command: " + args[0]);
///     })
///     .register(new HelpSubCommand())
///     .register(new TeleportSubCommand())
///     .build();
/// ```
///
/// For platform-specific usage, prefer using `BukkitCommandDispatcher.builder()`
/// or `VelocityCommandDispatcher.builder()` which provide platform-specific features.
///
/// @param <S> the command source type
/// @see SubCommand
public class CommandDispatcher<S> {

    private final List<SubCommand<S>> subCommands;
    private final String defaultSubCommand;
    private final BiConsumer<S, String[]> unknownSubCommandHandler;
    private final BiConsumer<S, String[]> noArgsHandler;
    private final BiConsumer<S, SubCommand<S>> permissionDeniedHandler;
    private final BiPredicate<S, String> permissionChecker;

    /// Creates a new CommandDispatcher with the specified builder configuration.
    ///
    /// @param builder the builder containing configuration
    protected CommandDispatcher(Builder<S> builder) {
        this.subCommands = new ArrayList<>(builder.subCommands);
        this.defaultSubCommand = builder.defaultSubCommand;
        this.unknownSubCommandHandler = builder.unknownSubCommandHandler;
        this.noArgsHandler = builder.noArgsHandler;
        this.permissionDeniedHandler = builder.permissionDeniedHandler;
        this.permissionChecker = builder.permissionChecker;
    }

    /// Dispatches a command to the appropriate subcommand.
    ///
    /// @param source the command source
    /// @param args   the command arguments
    /// @return true if the command was handled
    public boolean dispatch(@NotNull S source, @NotNull String[] args) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(args, "args");

        if (args.length == 0) {
            if (defaultSubCommand != null) {
                Optional<SubCommand<S>> sub = findSubCommand(defaultSubCommand);
                if (sub.isPresent()) {
                    executeSubCommand(source, sub.get(), args);
                    return true;
                }
            }
            if (noArgsHandler != null) {
                noArgsHandler.accept(source, args);
            }
            return true;
        }

        Optional<SubCommand<S>> subCommand = findSubCommand(args[0]);
        if (subCommand.isPresent()) {
            SubCommand<S> sc = subCommand.get();
            String permission = sc.getPermission();
            if (permission != null && permissionChecker != null && !permissionChecker.test(source, permission)) {
                if (permissionDeniedHandler != null) {
                    permissionDeniedHandler.accept(source, sc);
                }
                return true;
            }
            executeSubCommand(source, sc, args);
        } else if (unknownSubCommandHandler != null) {
            unknownSubCommandHandler.accept(source, args);
        }

        return true;
    }

    /// Executes a subcommand. Can be overridden for custom behavior.
    ///
    /// @param source     the command source
    /// @param subCommand the subcommand to execute
    /// @param args       the command arguments
    protected void executeSubCommand(@NotNull S source, @NotNull SubCommand<S> subCommand, @NotNull String[] args) {
        subCommand.execute(source, args);
    }

    /// Returns tab completion suggestions for the command.
    ///
    /// @param source the command source
    /// @param args   the current command arguments
    /// @return a list of suggestions
    public @NotNull List<String> tabComplete(@NotNull S source, @NotNull String[] args) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(args, "args");

        List<String> completions = new ArrayList<>();

        if (args.length <= 1) {
            String input = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
            for (SubCommand<S> sc : subCommands) {
                if (sc.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                    completions.add(sc.getName());
                }
                for (String alias : sc.getAliases()) {
                    if (alias.toLowerCase(Locale.ROOT).startsWith(input)) {
                        completions.add(alias);
                    }
                }
            }
        } else {
            Optional<SubCommand<S>> subCommand = findSubCommand(args[0]);
            subCommand.ifPresent(sc -> completions.addAll(sc.tabComplete(source, args)));
        }

        return completions;
    }

    /// Finds a subcommand by name or alias.
    ///
    /// @param name the name or alias to search for
    /// @return an Optional containing the subcommand if found
    public @NotNull Optional<SubCommand<S>> findSubCommand(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        for (SubCommand<S> sc : subCommands) {
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
    public @NotNull List<SubCommand<S>> getSubCommands() {
        return List.copyOf(subCommands);
    }

    /// Builder for creating [CommandDispatcher] instances.
    ///
    /// @param <S> the command source type
    public static class Builder<S> {

        private final List<SubCommand<S>> subCommands = new ArrayList<>();
        private String defaultSubCommand;
        private BiConsumer<S, String[]> unknownSubCommandHandler;
        private BiConsumer<S, String[]> noArgsHandler;
        private BiConsumer<S, SubCommand<S>> permissionDeniedHandler;
        private BiPredicate<S, String> permissionChecker;

        /// Creates a new builder.
        protected Builder() {}

        /// Registers a subcommand with the dispatcher.
        ///
        /// @param subCommand the subcommand to register
        /// @return this builder
        public @NotNull Builder<S> register(@NotNull SubCommand<S> subCommand) {
            this.subCommands.add(Objects.requireNonNull(subCommand, "subCommand"));
            return this;
        }

        /// Registers multiple subcommands with the dispatcher.
        ///
        /// @param subCommands the subcommands to register
        /// @return this builder
        @SafeVarargs
        public final @NotNull Builder<S> registerAll(@NotNull SubCommand<S>... subCommands) {
            for (SubCommand<S> sc : subCommands) {
                register(sc);
            }
            return this;
        }

        /// Sets the default subcommand to execute when no arguments are provided.
        ///
        /// @param subCommandName the name of the default subcommand
        /// @return this builder
        public @NotNull Builder<S> defaultSubCommand(@Nullable String subCommandName) {
            this.defaultSubCommand = subCommandName;
            return this;
        }

        /// Sets the handler for unknown subcommands.
        ///
        /// @param handler the handler to invoke when an unknown subcommand is used
        /// @return this builder
        public @NotNull Builder<S> unknownSubCommandHandler(@Nullable BiConsumer<S, String[]> handler) {
            this.unknownSubCommandHandler = handler;
            return this;
        }

        /// Sets the handler for when no arguments are provided and no default is set.
        ///
        /// @param handler the handler to invoke
        /// @return this builder
        public @NotNull Builder<S> noArgsHandler(@Nullable BiConsumer<S, String[]> handler) {
            this.noArgsHandler = handler;
            return this;
        }

        /// Sets the handler for permission denied scenarios.
        ///
        /// @param handler the handler to invoke when permission is denied
        /// @return this builder
        public @NotNull Builder<S> permissionDeniedHandler(@Nullable BiConsumer<S, SubCommand<S>> handler) {
            this.permissionDeniedHandler = handler;
            return this;
        }

        /// Sets the permission checker function.
        ///
        /// @param checker the function to check permissions (source, permission) -> hasPermission
        /// @return this builder
        public @NotNull Builder<S> permissionChecker(@Nullable BiPredicate<S, String> checker) {
            this.permissionChecker = checker;
            return this;
        }

        /// Builds the CommandDispatcher.
        ///
        /// @return a new CommandDispatcher instance
        public @NotNull CommandDispatcher<S> build() {
            return new CommandDispatcher<>(this);
        }
    }
}
