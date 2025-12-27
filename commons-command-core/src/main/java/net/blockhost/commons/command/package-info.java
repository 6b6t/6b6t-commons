/// Core command framework for 6b6t plugins.
///
/// This package provides platform-independent abstractions for building
/// command systems with subcommands. Platform-specific implementations
/// are provided in separate modules:
///
/// - `commons-command-bukkit` for Paper/Bukkit servers
/// - `commons-command-velocity` for Velocity proxies
///
/// @see net.blockhost.commons.command.SubCommand
/// @see net.blockhost.commons.command.CommandDispatcher
@NullMarked
package net.blockhost.commons.command;

import org.jspecify.annotations.NullMarked;
