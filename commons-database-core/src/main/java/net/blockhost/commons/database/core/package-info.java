/// Core database utilities for 6b6t plugins.
///
/// This package provides dialect-independent database connectivity using HikariCP
/// connection pooling. It provides a unified API for connection management that
/// works with any SQL database.
///
/// ## Quick Start with SQLManager (Recommended)
///
/// Use the database-specific modules (commons-database-mariadb, commons-database-postgres)
/// for configuration that provides the JDBC URL and driver class:
///
/// ```java
/// // Using MariaDbConfig from commons-database-mariadb
/// MariaDbConfig dbConfig = config.database();
///
/// SQLManager sqlManager = SQLManager.builder()
///     .credentials(dbConfig.toCredentials())
///     .maxPoolSize(dbConfig.maxPoolSize())
///     .minIdle(dbConfig.minIdle())
///     .poolName("MyPlugin-Pool")
///     .logger(plugin.getLogger())
///     .build();
///
/// sqlManager.registerTable("CREATE TABLE IF NOT EXISTS players (...)");
/// sqlManager.connect();
///
/// sqlManager.withConnection(connection -> {
///     // Use connection
/// });
///
/// sqlManager.shutdown();
/// ```
///
/// ## Database-Specific Modules
///
/// For database-specific configuration, use the specialized modules:
/// - `commons-database-mariadb` - MariaDB/MySQL configuration
/// - `commons-database-postgres` - PostgreSQL configuration
/// - `commons-database-redis` - Redis connectivity
///
/// @see net.blockhost.commons.database.core.SQLManager
/// @see net.blockhost.commons.database.core.DatabaseCredentials
/// @see net.blockhost.commons.database.core.HikariDataSourceBuilder
@NullMarked
package net.blockhost.commons.database.core;

import org.jspecify.annotations.NullMarked;
