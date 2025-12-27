package net.blockhost.commons.command.bukkit;

import net.blockhost.commons.command.SubCommand;
import org.bukkit.entity.Player;

/// Bukkit-specific subcommand interface for player commands.
///
/// This is a convenience interface that pre-specifies [Player] as the
/// command source type for Bukkit plugins.
///
/// Example implementation:
/// ```java
/// public class TeleportSubCommand implements BukkitSubCommand {
///     @Override
///     public void execute(@NotNull Player player, @NotNull String[] args) {
///         // Handle teleport command
///     }
///
///     @Override
///     public @NotNull String getName() {
///         return "teleport";
///     }
/// }
/// ```
///
/// @see BukkitCommandDispatcher
/// @see SubCommand
public interface BukkitSubCommand extends SubCommand<Player> {}
