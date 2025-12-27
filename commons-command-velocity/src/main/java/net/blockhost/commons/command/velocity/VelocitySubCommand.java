package net.blockhost.commons.command.velocity;

import com.velocitypowered.api.command.CommandSource;
import net.blockhost.commons.command.SubCommand;

/// Velocity-specific subcommand interface.
///
/// This is a convenience interface that pre-specifies [CommandSource] as the
/// command source type for Velocity plugins.
///
/// Example implementation:
/// ```java
/// public class ServerSubCommand implements VelocitySubCommand {
///     @Override
///     public void execute(@NotNull CommandSource source, @NotNull String[] args) {
///         // Handle server command
///     }
///
///     @Override
///     public @NotNull String getName() {
///         return "server";
///     }
/// }
/// ```
///
/// @see VelocityCommandDispatcher
/// @see SubCommand
public interface VelocitySubCommand extends SubCommand<CommandSource> {}
