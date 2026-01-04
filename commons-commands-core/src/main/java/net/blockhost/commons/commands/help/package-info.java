/// Help system for command documentation.
///
/// This package provides utilities for formatting and displaying
/// command help messages using MiniMessage formatting.
///
/// ## Quick Start
/// ```java
/// // Create formatter with config
/// HelpFormatter formatter = new HelpFormatter(config.help());
///
/// // Send help to a player
/// formatter.sendHelp(player, List.of(
///     new HelpFormatter.CommandEntry("spawn", "Teleport to spawn"),
///     new HelpFormatter.CommandEntry("home", "Teleport to your home")
/// ));
/// ```
///
/// @see net.blockhost.commons.commands.help.HelpConfig
/// @see net.blockhost.commons.commands.help.HelpFormatter
@NullMarked
package net.blockhost.commons.commands.help;

import org.jspecify.annotations.NullMarked;
