/**
 * Command framework for 6b6t plugins.
 *
 * <p>This package provides a simple command framework based on the subcommand pattern.
 * It allows you to create commands with multiple subcommands, each handling their own
 * execution and tab completion logic.
 *
 * <h2>Quick Start</h2>
 *
 * <p>Create a subcommand by implementing {@link net.blockhost.commons.command.SubCommand}:
 * <pre>{@code
 * public class HelpSubCommand implements SubCommand {
 *     @Override
 *     public void execute(@NotNull Player player, @NotNull String[] args) {
 *         player.sendMessage("Available commands: ...");
 *     }
 *
 *     @Override
 *     public @NotNull String getName() {
 *         return "help";
 *     }
 *
 *     @Override
 *     public @NotNull String[] getAliases() {
 *         return new String[]{"?", "h"};
 *     }
 * }
 * }</pre>
 *
 * <p>Create and register the dispatcher:
 * <pre>{@code
 * CommandDispatcher dispatcher = CommandDispatcher.builder()
 *     .defaultSubCommand("help")
 *     .unknownSubCommandHandler((player, args) ->
 *         player.sendMessage("Unknown command: " + args[0]))
 *     .register(new HelpSubCommand())
 *     .register(new TeleportSubCommand())
 *     .build();
 *
 * plugin.getCommand("mycommand").setExecutor(dispatcher);
 * plugin.getCommand("mycommand").setTabCompleter(dispatcher);
 * }</pre>
 *
 * @see net.blockhost.commons.command.SubCommand
 * @see net.blockhost.commons.command.CommandDispatcher
 */
@NullMarked
package net.blockhost.commons.command;

import org.jspecify.annotations.NullMarked;
