/**
 * Configuration utilities for 6b6t plugins.
 *
 * <p>This package provides utilities for loading and managing YAML configuration files
 * using the ConfigLib library. It simplifies common configuration patterns and provides
 * a consistent API across plugins.
 *
 * <h2>Quick Start</h2>
 *
 * <p>Define your configuration class using ConfigLib annotations:
 * <pre>{@code
 * import de.exlll.configlib.Comment;
 * import de.exlll.configlib.Configuration;
 *
 * @Configuration
 * public class MyPluginConfig {
 *     @Comment("The server host address")
 *     private String host = "localhost";
 *
 *     @Comment("The server port")
 *     private int port = 3306;
 *
 *     @Comment("Database settings")
 *     private DatabaseSettings database = new DatabaseSettings();
 *
 *     // Getters...
 * }
 * }</pre>
 *
 * <p>Load your configuration:
 * <pre>{@code
 * Path configPath = plugin.getDataFolder().toPath().resolve("config.yml");
 * MyPluginConfig config = ConfigLoader.loadOrCreate(configPath, MyPluginConfig.class);
 * }</pre>
 *
 * @see net.blockhost.commons.config.ConfigLoader
 * @see de.exlll.configlib.Configuration
 */
@NullMarked
package net.blockhost.commons.config;

import org.jspecify.annotations.NullMarked;
