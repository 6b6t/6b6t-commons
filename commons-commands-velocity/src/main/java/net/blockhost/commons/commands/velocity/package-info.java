/**
 * StrokkCommands integration for Velocity plugins.
 *
 * <p>This module provides StrokkCommands v2.0.0 annotation-based command framework
 * for creating Brigadier commands in Velocity proxy plugins. Commands are defined
 * using annotations and compiled at build time, resulting in zero runtime overhead.</p>
 *
 * <h2>Usage</h2>
 * <p>Add this module as a dependency and use the StrokkCommands annotations:</p>
 * <pre>{@code
 * @Command("example")
 * @Description("An example command")
 * public class ExampleCommand {
 *
 *     @Executes
 *     void execute(CommandSource source) {
 *         source.sendMessage(Component.text("Hello from StrokkCommands!").color(NamedTextColor.GREEN));
 *     }
 *
 *     @Executes("server")
 *     void server(CommandSource source, @Literal({"lobby", "survival", "creative"}) String server) {
 *         source.sendMessage(Component.text("Connecting to " + server + "..."));
 *     }
 * }
 * }</pre>
 *
 * <p>Register the generated command in your Velocity plugin.</p>
 *
 * @see <a href="https://commands.strokkur.net/docs/">StrokkCommands Documentation</a>
 */
@NullMarked
package net.blockhost.commons.commands.velocity;

import org.jspecify.annotations.NullMarked;
