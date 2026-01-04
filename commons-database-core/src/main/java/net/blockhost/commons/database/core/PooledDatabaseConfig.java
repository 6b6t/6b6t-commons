package net.blockhost.commons.database.core;

/// Interface for database configurations that support HikariCP connection pooling.
///
/// This interface provides a common contract for database configurations across
/// different database dialects (MariaDB, PostgreSQL, etc.). Implementing this
/// interface allows configurations to be used directly with [SQLManager].
///
/// ## Example Usage
/// ```java
/// // MariaDbConfig implements PooledDatabaseConfig
/// MariaDbConfig config = new MariaDbConfig();
///
/// // Can be used directly with SQLManager
/// SQLManager sqlManager = SQLManager.create(config)
///     .poolName("MyPlugin-Pool")
///     .logger(logger)
///     .build();
/// ```
///
/// @see SQLManager
/// @see DatabaseCredentials
public interface PooledDatabaseConfig {

    /// Converts this configuration to database credentials.
    ///
    /// @return the database credentials for connection
    DatabaseCredentials toCredentials();

    /// Gets the maximum number of connections in the pool.
    ///
    /// @return the maximum pool size
    int maxPoolSize();

    /// Gets the minimum number of idle connections in the pool.
    ///
    /// @return the minimum idle connections
    int minIdle();
}
