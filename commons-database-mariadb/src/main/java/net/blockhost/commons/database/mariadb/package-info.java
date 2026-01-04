/// MariaDB database utilities for 6b6t plugins.
///
/// This package provides MariaDB-specific configuration with HikariCP
/// connection pooling. Use [MariaDbConfig] in your plugin configuration
/// and convert to credentials for the core [net.blockhost.commons.database.core.SQLManager].
///
/// ## Quick Start
///
/// ```java
/// // Using ConfigLib configuration
/// @Configuration
/// public class PluginConfig {
///     private MariaDbConfig database = new MariaDbConfig();
///     public MariaDbConfig database() { return database; }
/// }
///
/// // Create SQLManager from config
/// SQLManager sqlManager = SQLManager.builder()
///     .credentials(config.database().toCredentials())
///     .maxPoolSize(config.database().maxPoolSize())
///     .minIdle(config.database().minIdle())
///     .poolName("MyPlugin-Pool")
///     .logger(plugin.getLogger())
///     .build();
///
/// sqlManager.connect();
/// ```
///
/// @see net.blockhost.commons.database.mariadb.MariaDbConfig
/// @see net.blockhost.commons.database.core.SQLManager
@NullMarked
package net.blockhost.commons.database.mariadb;

import org.jspecify.annotations.NullMarked;
