/**
 * StrokkCommands integration for Bukkit/Paper plugins.
 *
 * <p>This module provides StrokkCommands v2.0.0 annotation-based command framework
 * for creating Brigadier commands in Paper plugins. Commands are defined using
 * annotations and compiled at build time, resulting in zero runtime overhead.</p>
 *
 * <h2>Usage</h2>
 * <p>Add this module as a dependency and use the StrokkCommands annotations:</p>
 * <pre>{@code
 * @Command("example")
 * @Description("An example command")
 * public class ExampleCommand {
 *
 *     @Executes
 *     void execute(CommandSender sender) {
 *         sender.sendRichMessage("<green>Hello from StrokkCommands!");
 *     }
 *
 *     @Executes("greet")
 *     void greet(CommandSender sender, Player target) {
 *         sender.sendRichMessage("<yellow>Hello, " + target.getName() + "!");
 *     }
 * }
 * }</pre>
 *
 * <p>Register the generated command in your plugin:</p>
 * <pre>{@code
 * public void onLoad() {
 *     getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
 *         ExampleCommandBrigadier.register(event.registrar());
 *     }));
 * }
 * }</pre>
 *
 * @see <a href="https://commands.strokkur.net/docs/">StrokkCommands Documentation</a>
 */
@NullMarked
package net.blockhost.commons.commands.bukkit;

import org.jspecify.annotations.NullMarked;
