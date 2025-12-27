/// Configuration utilities for 6b6t plugins.
///
/// This package provides utilities for loading and managing YAML configuration files
/// using the ConfigLib library. It simplifies common configuration patterns and provides
/// a consistent API across plugins.
/// ## Quick Start
///
/// Define your configuration class using ConfigLib annotations:
/// <pre>
/// `import de.exlll.configlib.Comment;import de.exlll.configlib.Configuration;class MyPluginConfig{host address")private String host = "localhost";port")private int port = 3306;")private DatabaseSettings database = new DatabaseSettings();// Getters...}`</pre>
///
/// Load your configuration:
/// <pre>
/// `Path configPath = plugin.getDataFolder().toPath().resolve("config.yml");MyPluginConfig config = ConfigLoader.loadOrCreate(configPath, MyPluginConfig.class);`</pre>
///
/// @see net.blockhost.commons.config.ConfigLoader
/// @see de.exlll.configlib.Configuration
@NullMarked
package net.blockhost.commons.config;

import org.jspecify.annotations.NullMarked;
