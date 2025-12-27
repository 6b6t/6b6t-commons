package net.blockhost.commons.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/// Interface for implementing subcommands in a command hierarchy.
///
/// This interface defines the contract for subcommands that can be registered
/// with a [CommandDispatcher]. Each subcommand has a name, optional aliases,
/// and handles command execution and tab completion.
///
/// Example implementation:
/// <pre>
/// `public class TeleportSubCommand implements SubCommand{void execute(@NotNull Player player, @NotNull String[] args){// Handle teleport command}@NotNull String getName(){return "teleport";}@NotNull String[] getAliases(){return new String[]{"tp", "goto"};}@NotNull List<String> tabComplete(@NotNull Player player, @NotNull String[] args){if (args.length == 2){return getOnlinePlayerNames();}return List.of();}}`</pre>
///
/// @see CommandDispatcher
public interface SubCommand {

    /// Executes the subcommand for the given player.
    ///
    /// The args array includes all arguments passed to the parent command,
    /// starting with the subcommand name at index 0.
    ///
    /// @param player the player executing the command
    /// @param args   the command arguments (including subcommand name at index 0)
    void execute(@NotNull Player player, @NotNull String[] args);

    /// Returns the primary name of this subcommand.
    ///
    /// This name is used for matching user input and should be lowercase.
    ///
    /// @return the subcommand name
    @NotNull
    String getName();

    /// Returns alternative names (aliases) for this subcommand.
    ///
    /// Users can use any of these aliases interchangeably with the primary name.
    ///
    /// @return an array of aliases, may be empty but never null
    default @NotNull String[] getAliases() {
        return new String[0];
    }

    /// Returns tab completion suggestions for this subcommand.
    ///
    /// The args array includes all arguments passed to the parent command.
    /// Implementations should check the args length to determine which argument
    /// is being completed.
    ///
    /// @param player the player requesting tab completion
    /// @param args   the current command arguments
    /// @return a list of suggestions, may be empty but never null
    default @NotNull List<String> tabComplete(@NotNull Player player, @NotNull String[] args) {
        return List.of();
    }

    /// Returns the permission required to execute this subcommand.
    ///
    /// Return null if no specific permission is required.
    ///
    /// @return the permission node, or null if none required
    default String getPermission() {
        return null;
    }

    /// Returns a brief description of what this subcommand does.
    ///
    /// This can be used in help messages.
    ///
    /// @return a description of the subcommand
    default @NotNull String getDescription() {
        return "";
    }

    /// Returns the usage syntax for this subcommand.
    ///
    /// This should show the expected arguments, e.g., `"<player> [message]"`.
    ///
    /// @return the usage syntax
    default String getUsage() {
        return "";
    }
}
