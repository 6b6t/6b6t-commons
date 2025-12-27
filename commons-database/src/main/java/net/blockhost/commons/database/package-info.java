/**
 * Database utilities for 6b6t plugins.
 *
 * <p>This package provides utilities for connecting to MariaDB databases,
 * including connection factories and HikariCP connection pool builders.
 *
 * <h2>Quick Start with ConfigLib</h2>
 *
 * <p>The easiest way to add database support to your plugin is to embed
 * {@link net.blockhost.commons.database.DatabaseConfig} in your configuration:
 * <pre>{@code
 * @Configuration
 * public class PluginConfig {
 *     @Comment("Database connection settings")
 *     private DatabaseConfig database = new DatabaseConfig();
 *
 *     public DatabaseConfig database() {
 *         return database;
 *     }
 * }
 *
 * // Load config and create connection pool
 * PluginConfig config = ConfigLoader.loadOrCreate(configPath, PluginConfig.class);
 * HikariDataSource dataSource = HikariDataSourceBuilder.createDataSource(config.database());
 * }</pre>
 *
 * <h2>Manual Configuration</h2>
 *
 * <p>For simple, non-pooled connections:
 * <pre>{@code
 * DatabaseCredentials credentials = DatabaseCredentials.builder()
 *     .host("localhost")
 *     .port(3306)
 *     .database("mydb")
 *     .username("user")
 *     .password("pass")
 *     .build();
 *
 * try (Connection conn = MariaDbConnectionFactory.openConnection(credentials)) {
 *     // Use the connection
 * }
 * }</pre>
 *
 * <p>For connection pooling with HikariCP:
 * <pre>{@code
 * HikariDataSource dataSource = HikariDataSourceBuilder.create(credentials)
 *     .poolName("MyPlugin-Pool")
 *     .maximumPoolSize(10)
 *     .build();
 *
 * try (Connection conn = dataSource.getConnection()) {
 *     // Use the connection
 * }
 *
 * // Remember to close the data source when done
 * dataSource.close();
 * }</pre>
 *
 * @see net.blockhost.commons.database.DatabaseConfig
 * @see net.blockhost.commons.database.DatabaseCredentials
 * @see net.blockhost.commons.database.MariaDbConnectionFactory
 * @see net.blockhost.commons.database.HikariDataSourceBuilder
 */
@NullMarked
package net.blockhost.commons.database;

import org.jspecify.annotations.NullMarked;
