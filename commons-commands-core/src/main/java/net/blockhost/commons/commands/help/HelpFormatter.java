package net.blockhost.commons.commands.help;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Objects;

/// Formats and sends help messages for commands.
///
/// This class uses MiniMessage formatting to display help information
/// based on the provided {@link HelpConfig} configuration.
///
/// ## Example Usage
/// ```java
/// HelpFormatter formatter = new HelpFormatter(helpConfig);
/// formatter.sendHelp(player, List.of(
///     new CommandEntry("spawn", "Teleport to spawn"),
///     new CommandEntry("home", "Teleport to your home")
/// ));
/// ```
public final class HelpFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final HelpConfig config;

    /// Creates a new help formatter with the given configuration.
    ///
    /// @param config the help configuration
    public HelpFormatter(HelpConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    /// Sends a help message to the given audience.
    ///
    /// @param audience the audience to send the help to
    /// @param commands the list of commands to display
    public void sendHelp(Audience audience, List<CommandEntry> commands) {
        Objects.requireNonNull(audience, "audience");
        Objects.requireNonNull(commands, "commands");

        // Header
        if (!config.header.isEmpty()) {
            audience.sendMessage(MINI_MESSAGE.deserialize(config.header));
        }

        // Separator after header
        if (!config.separator.isEmpty() && !config.header.isEmpty()) {
            audience.sendMessage(MINI_MESSAGE.deserialize(config.separator));
        }

        // Commands
        if (commands.isEmpty()) {
            audience.sendMessage(MINI_MESSAGE.deserialize(config.noCommands));
        } else {
            for (CommandEntry entry : commands) {
                Component line = MINI_MESSAGE.deserialize(
                        config.commandFormat,
                        Placeholder.unparsed("command", entry.command()),
                        Placeholder.unparsed("description", entry.description())
                );
                audience.sendMessage(line);
            }
        }

        // Separator before footer
        if (!config.separator.isEmpty() && !config.footer.isEmpty()) {
            audience.sendMessage(MINI_MESSAGE.deserialize(config.separator));
        }

        // Footer
        if (!config.footer.isEmpty()) {
            audience.sendMessage(MINI_MESSAGE.deserialize(config.footer));
        }
    }

    /// Sends a mock help message for testing purposes.
    ///
    /// @param audience the audience to send the help to
    public void sendMockHelp(Audience audience) {
        sendHelp(audience, List.of(
                new CommandEntry("help", "Shows this help message"),
                new CommandEntry("spawn", "Teleport to spawn point"),
                new CommandEntry("home", "Teleport to your home"),
                new CommandEntry("tpa <player>", "Request to teleport to a player")
        ));
    }

    /// Represents a command entry in the help listing.
    ///
    /// @param command the command name/syntax
    /// @param description the command description
    public record CommandEntry(String command, String description) {
        public CommandEntry {
            Objects.requireNonNull(command, "command");
            Objects.requireNonNull(description, "description");
        }
    }
}
