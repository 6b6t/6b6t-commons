package net.blockhost.commons.commands.velocity;

/**
 * Marker class for the StrokkCommands Velocity integration module.
 *
 * <p>This module provides StrokkCommands v2.0.0 annotation-based command framework
 * for creating Brigadier commands in Velocity proxy plugins. Commands are defined
 * using annotations and compiled at build time, resulting in zero runtime overhead.</p>
 *
 * <h2>Quick Start</h2>
 * <p>Add this module as a dependency to your plugin:</p>
 * <pre>{@code
 * dependencies {
 *     compileOnly("net.blockhost.commons:commons-commands-velocity:VERSION")
 *     annotationProcessor("net.strokkur.commands:processor-velocity:2.0.0-SNAPSHOT")
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
 * <h2>Registering Commands</h2>
 * <p>Register the generated command in your Velocity plugin.</p>
 *
 * @see <a href="https://commands.strokkur.net/docs/">StrokkCommands Documentation</a>
 */
public final class StrokkCommandsVelocity {

    /**
     * The version of StrokkCommands this module is built against.
     */
    public static final String STROKKCOMMANDS_VERSION = "2.0.0-SNAPSHOT";

    private StrokkCommandsVelocity() {
        // Utility class
    }
}
