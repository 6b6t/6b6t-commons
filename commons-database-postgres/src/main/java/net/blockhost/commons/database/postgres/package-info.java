/// PostgreSQL database utilities for 6b6t plugins.
///
/// This package provides PostgreSQL-specific configuration with HikariCP
/// connection pooling. Use [PostgresConfig] in your plugin configuration
/// and convert to credentials for the core [net.blockhost.commons.database.core.SQLManager].
///
/// ## Quick Start
///
/// ```java
/// // Using ConfigLib configuration
/// @Configuration
/// public class PluginConfig {
///     private PostgresConfig database = new PostgresConfig();
///     public PostgresConfig database() { return database; }
/// }
///
/// // Create SQLManager from config
/// SQLManager sqlManager = SQLManager.create(config.database())
///     .poolName("MyPlugin-Pool")
///     .logger(plugin.getLogger())
///     .build();
///
/// sqlManager.connect();
/// ```
///
/// @see net.blockhost.commons.database.postgres.PostgresConfig
/// @see net.blockhost.commons.database.core.SQLManager
@NullMarked
package net.blockhost.commons.database.postgres;

import org.jspecify.annotations.NullMarked;
