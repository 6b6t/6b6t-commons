package net.blockhost.commons.command.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.blockhost.commons.command.CommandDispatcher;
import net.blockhost.commons.command.SubCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/// Velocity-specific command dispatcher that implements [SimpleCommand].
///
/// This class integrates the generic [CommandDispatcher] with Velocity's command system.
///
/// Example usage:
/// ```java
/// VelocityCommandDispatcher dispatcher = VelocityCommandDispatcher.builder()
///     .defaultSubCommand("help")
///     .unknownSubCommandHandler((source, args) -> {
///         source.sendMessage(Component.text("Unknown command: " + args[0]));
///     })
///     .register(new HelpSubCommand())
///     .register(new ServerSubCommand())
///     .build();
///
/// // Register with Velocity
/// commandManager.register("mycommand", dispatcher);
/// ```
///
/// @see VelocitySubCommand
/// @see CommandDispatcher
public final class VelocityCommandDispatcher extends CommandDispatcher<CommandSource> implements SimpleCommand {

    private final BiConsumer<CommandSource, String[]> nonPlayerHandler;
    private final boolean playerOnly;

    private VelocityCommandDispatcher(Builder builder) {
        super(builder);
        this.nonPlayerHandler = builder.nonPlayerHandler;
        this.playerOnly = builder.playerOnly;
    }

    /// Creates a new builder for VelocityCommandDispatcher.
    ///
    /// @return a new builder instance
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (playerOnly && !(source instanceof Player)) {
            if (nonPlayerHandler != null) {
                nonPlayerHandler.accept(source, args);
            }
            return;
        }

        dispatch(source, args);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (playerOnly && !(source instanceof Player)) {
            return CompletableFuture.completedFuture(List.of());
        }

        return CompletableFuture.completedFuture(tabComplete(source, args));
    }

    /// Builder for creating [VelocityCommandDispatcher] instances.
    public static final class Builder extends CommandDispatcher.Builder<CommandSource> {

        private BiConsumer<CommandSource, String[]> nonPlayerHandler;
        private boolean playerOnly = false;

        private Builder() {
            // Set up default Velocity permission checker
            permissionChecker(CommandSource::hasPermission);
        }

        /// Sets whether this command is player-only.
        ///
        /// @param playerOnly true if only players can use this command
        /// @return this builder
        public @NotNull Builder playerOnly(boolean playerOnly) {
            this.playerOnly = playerOnly;
            return this;
        }

        /// Sets the handler for non-player command sources (when playerOnly is true).
        ///
        /// @param handler the handler to invoke when a non-player uses the command
        /// @return this builder
        public @NotNull Builder nonPlayerHandler(@Nullable BiConsumer<CommandSource, String[]> handler) {
            this.nonPlayerHandler = handler;
            return this;
        }

        @Override
        public @NotNull Builder register(@NotNull SubCommand<CommandSource> subCommand) {
            super.register(subCommand);
            return this;
        }

        @Override
        public @NotNull Builder defaultSubCommand(@Nullable String subCommandName) {
            super.defaultSubCommand(subCommandName);
            return this;
        }

        @Override
        public @NotNull Builder unknownSubCommandHandler(@Nullable BiConsumer<CommandSource, String[]> handler) {
            super.unknownSubCommandHandler(handler);
            return this;
        }

        @Override
        public @NotNull Builder noArgsHandler(@Nullable BiConsumer<CommandSource, String[]> handler) {
            super.noArgsHandler(handler);
            return this;
        }

        @Override
        public @NotNull Builder permissionDeniedHandler(
                @Nullable BiConsumer<CommandSource, SubCommand<CommandSource>> handler) {
            super.permissionDeniedHandler(handler);
            return this;
        }

        @Override
        public @NotNull VelocityCommandDispatcher build() {
            return new VelocityCommandDispatcher(this);
        }
    }
}
