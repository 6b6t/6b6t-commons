package net.blockhost.commons.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for implementing subcommands in a command hierarchy.
 *
 * <p>This interface defines the contract for subcommands that can be registered
 * with a {@link CommandDispatcher}. Each subcommand has a name, optional aliases,
 * and handles command execution and tab completion.
 *
 * <p>Example implementation:
 * <pre>{@code
 * public class TeleportSubCommand implements SubCommand {
 *     @Override
 *     public void execute(@NotNull Player player, @NotNull String[] args) {
 *         // Handle teleport command
 *     }
 *
 *     @Override
 *     public @NotNull String getName() {
 *         return "teleport";
 *     }
 *
 *     @Override
 *     public @NotNull String[] getAliases() {
 *         return new String[]{"tp", "goto"};
 *     }
 *
 *     @Override
 *     public @NotNull List<String> tabComplete(@NotNull Player player, @NotNull String[] args) {
 *         if (args.length == 2) {
 *             return getOnlinePlayerNames();
 *         }
 *         return List.of();
 *     }
 * }
 * }</pre>
 *
 * @see CommandDispatcher
 */
public interface SubCommand {

    /**
     * Executes the subcommand for the given player.
     *
     * <p>The args array includes all arguments passed to the parent command,
     * starting with the subcommand name at index 0.
     *
     * @param player the player executing the command
     * @param args   the command arguments (including subcommand name at index 0)
     */
    void execute(@NotNull Player player, @NotNull String[] args);

    /**
     * Returns the primary name of this subcommand.
     *
     * <p>This name is used for matching user input and should be lowercase.
     *
     * @return the subcommand name
     */
    @NotNull
    String getName();

    /**
     * Returns alternative names (aliases) for this subcommand.
     *
     * <p>Users can use any of these aliases interchangeably with the primary name.
     *
     * @return an array of aliases, may be empty but never null
     */
    default @NotNull String[] getAliases() {
        return new String[0];
    }

    /**
     * Returns tab completion suggestions for this subcommand.
     *
     * <p>The args array includes all arguments passed to the parent command.
     * Implementations should check the args length to determine which argument
     * is being completed.
     *
     * @param player the player requesting tab completion
     * @param args   the current command arguments
     * @return a list of suggestions, may be empty but never null
     */
    default @NotNull List<String> tabComplete(@NotNull Player player, @NotNull String[] args) {
        return List.of();
    }

    /**
     * Returns the permission required to execute this subcommand.
     *
     * <p>Return null if no specific permission is required.
     *
     * @return the permission node, or null if none required
     */
    default String getPermission() {
        return null;
    }

    /**
     * Returns a brief description of what this subcommand does.
     *
     * <p>This can be used in help messages.
     *
     * @return a description of the subcommand
     */
    default @NotNull String getDescription() {
        return "";
    }

    /**
     * Returns the usage syntax for this subcommand.
     *
     * <p>This should show the expected arguments, e.g., {@code "<player> [message]"}.
     *
     * @return the usage syntax
     */
    default @NotNull String getUsage() {
        return "";
    }
}
