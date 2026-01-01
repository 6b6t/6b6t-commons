package net.blockhost.commons.commands.bukkit;

/**
 * Marker class for the StrokkCommands Bukkit integration module.
 *
 * <p>This module provides StrokkCommands v2.0.0 annotation-based command framework
 * for creating Brigadier commands in Paper plugins. Commands are defined using
 * annotations and compiled at build time, resulting in zero runtime overhead.</p>
 *
 * <h2>Quick Start</h2>
 * <p>Add this module as a dependency to your plugin:</p>
 * <pre>{@code
 * dependencies {
 *     compileOnly("net.blockhost.commons:commons-commands-bukkit:VERSION")
 *     annotationProcessor("net.strokkur.commands:processor-paper:2.0.0-SNAPSHOT")
 * }
 * }</pre>
 *
 * <h2>Creating Commands</h2>
 * <p>Define commands using StrokkCommands annotations:</p>
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
 * <h2>Registering Commands</h2>
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
public final class StrokkCommandsBukkit {

    /**
     * The version of StrokkCommands this module is built against.
     */
    public static final String STROKKCOMMANDS_VERSION = "2.0.0-SNAPSHOT";

    private StrokkCommandsBukkit() {
        // Utility class
    }
}
