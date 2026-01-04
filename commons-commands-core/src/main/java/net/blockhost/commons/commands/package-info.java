/// Core command utilities for StrokkCommands integration.
///
/// This module provides platform-independent utilities and base classes
/// for building commands with StrokkCommands. It includes the Brigadier
/// command framework, configuration support, and help message formatting.
///
/// ## Help System
/// The help subpackage provides configurable help message formatting:
/// ```java
/// HelpFormatter formatter = new HelpFormatter(config.help());
/// formatter.sendHelp(player, commands);
/// ```
///
/// @see net.blockhost.commons.commands.help.HelpConfig
/// @see net.blockhost.commons.commands.help.HelpFormatter
/// @see <a href="https://commands.strokkur.net/docs/">StrokkCommands Documentation</a>
@NullMarked
package net.blockhost.commons.commands;

import org.jspecify.annotations.NullMarked;
