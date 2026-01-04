package net.blockhost.commons.commands.help;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

/// Configuration for command help message formatting.
///
/// This configuration controls how help messages are displayed to users,
/// including colors, formatting, and message templates.
@Configuration
public class HelpConfig {

    @Comment("Header shown at the top of help messages")
    public String header = "<gold><bold>Command Help</bold></gold>";

    @Comment("Format for each command entry. Placeholders: <command>, <description>")
    public String commandFormat = "<yellow>/<command></yellow> <dark_gray>-</dark_gray> <gray><description></gray>";

    @Comment("Footer shown at the bottom of help messages")
    public String footer = "<dark_gray>Use /<command> help for more info</dark_gray>";

    @Comment("Message shown when no commands are available")
    public String noCommands = "<red>No commands available.</red>";

    @Comment("Separator between header/commands/footer")
    public String separator = "";
}
