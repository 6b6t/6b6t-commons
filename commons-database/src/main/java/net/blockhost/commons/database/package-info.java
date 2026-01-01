/// Database utilities for 6b6t plugins.
///
/// This package provides utilities for connecting to MariaDB databases,
/// including connection factories, HikariCP connection pool builders, and
/// a full-featured SQL manager with connection pooling, configuration reloads,
/// reconnection handling, and table migrations.
///
/// ## Quick Start with SQLManager (Recommended)
///
/// The [net.blockhost.commons.database.SQLManager] provides a complete solution
/// for database management in plugins:
/// ```java
/// SQLManager sqlManager = SQLManager.builder()
///     .config(databaseConfig)
///     .poolName("MyPlugin-Pool")
///     .logger(plugin.getLogger())
///     .build();
///
/// // Register tables
/// sqlManager.registerTable("CREATE TABLE IF NOT EXISTS players (...)");
///
/// // Connect
/// sqlManager.connect();
///
/// // Use connections (returns null on error, no exception)
/// Connection conn = sqlManager.getConnection();
/// if (conn != null) { ... }
///
/// // Or use safe callback pattern
/// sqlManager.withConnection(connection -> {
///     // Use connection
/// });
///
/// // Reload config at runtime
/// sqlManager.reload(newConfig);
///
/// // Shutdown
/// sqlManager.shutdown();
/// ```
///
/// ## ConfigLib Integration
///
/// Embed [net.blockhost.commons.database.DatabaseConfig] in your plugin config:
/// ```java
/// @Configuration
/// public class PluginConfig {
///     private DatabaseConfig database = new DatabaseConfig();
///     public DatabaseConfig database() { return database; }
/// }
/// ```
///
/// ## Low-Level Access
///
/// For simple, non-pooled connections use [net.blockhost.commons.database.MariaDbConnectionFactory].
/// For direct HikariCP pool creation use [net.blockhost.commons.database.HikariDataSourceBuilder].
///
/// @see net.blockhost.commons.database.SQLManager
/// @see net.blockhost.commons.database.DatabaseConfig
/// @see net.blockhost.commons.database.DatabaseCredentials
/// @see net.blockhost.commons.database.MariaDbConnectionFactory
/// @see net.blockhost.commons.database.HikariDataSourceBuilder
@NullMarked
package net.blockhost.commons.database;

import org.jspecify.annotations.NullMarked;
