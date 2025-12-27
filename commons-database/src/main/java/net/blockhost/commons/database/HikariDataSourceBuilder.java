package net.blockhost.commons.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// Builder for creating HikariCP connection pool data sources.
///
/// This builder provides a fluent API for configuring HikariCP connection pools
/// with sensible defaults for MariaDB connections. It integrates with
/// [DatabaseCredentials] for easy configuration.
///
/// Example usage:
/// <pre>
/// `DatabaseCredentials credentials =
// DatabaseCredentials.builder().host("localhost").database("mydb").username("user").password("pass").build();HikariDataSource dataSource = HikariDataSourceBuilder.create(credentials).poolName("MyApp-Pool").maximumPoolSize(10).build();`</pre>
///
/// @see DatabaseCredentials
/// @see HikariDataSource
public final class HikariDataSourceBuilder {

    private static final String DRIVER_CLASS = "org.mariadb.jdbc.Driver";

    private final DatabaseCredentials credentials;

    private String poolName;
    private int maximumPoolSize = 10;
    private int minimumIdle = 2;
    private Duration maxLifetime = Duration.ofMinutes(30);
    private Duration idleTimeout = Duration.ofMinutes(10);
    private Duration connectionTimeout = Duration.ofSeconds(30);
    private Duration validationTimeout = Duration.ofSeconds(5);
    private boolean cachePrepStmts = true;
    private int prepStmtCacheSize = 250;
    private int prepStmtCacheSqlLimit = 2048;
    private boolean autoReconnect = true;

    private HikariDataSourceBuilder(DatabaseCredentials credentials) {
        this.credentials = Objects.requireNonNull(credentials, "credentials");
        this.connectionTimeout = credentials.connectionTimeout();
    }

    /// Creates a new builder with the specified credentials.
    ///
    /// @param credentials the database credentials
    /// @return a new builder instance
    public static HikariDataSourceBuilder create(DatabaseCredentials credentials) {
        return new HikariDataSourceBuilder(credentials);
    }

    /// Creates a new builder from a [DatabaseConfig].
    ///
    /// This is a convenience method that converts the config to credentials
    /// and applies the pool size settings from the config.
    ///
    /// @param config the database configuration
    /// @return a new builder instance with pool settings applied
    public static HikariDataSourceBuilder create(DatabaseConfig config) {
        Objects.requireNonNull(config, "config");
        return new HikariDataSourceBuilder(config.toCredentials())
                .maximumPoolSize(config.maxPoolSize())
                .minimumIdle(config.minIdle());
    }

    /// Creates and builds a HikariDataSource directly from a [DatabaseConfig].
    ///
    /// This is a convenience method for quick setup when default settings are acceptable.
    ///
    /// @param config the database configuration
    /// @return a new HikariDataSource instance
    public static HikariDataSource createDataSource(DatabaseConfig config) {
        return create(config).build();
    }

    /// Sets the name of the connection pool.
    ///
    /// This name will appear in thread names and JMX metrics.
    ///
    /// @param poolName the pool name
    /// @return this builder
    public HikariDataSourceBuilder poolName(String poolName) {
        this.poolName = Objects.requireNonNull(poolName, "poolName");
        return this;
    }

    /// Sets the maximum size of the connection pool.
    ///
    /// @param size the maximum pool size (must be at least 1)
    /// @return this builder
    public HikariDataSourceBuilder maximumPoolSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Maximum pool size must be at least 1: " + size);
        }
        this.maximumPoolSize = size;
        return this;
    }

    /// Sets the minimum number of idle connections in the pool.
    ///
    /// @param size the minimum idle connections (must be non-negative)
    /// @return this builder
    public HikariDataSourceBuilder minimumIdle(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Minimum idle must be non-negative: " + size);
        }
        this.minimumIdle = size;
        return this;
    }

    /// Sets the maximum lifetime of a connection in the pool.
    ///
    /// @param maxLifetime the maximum lifetime
    /// @return this builder
    public HikariDataSourceBuilder maxLifetime(Duration maxLifetime) {
        this.maxLifetime = Objects.requireNonNull(maxLifetime, "maxLifetime");
        return this;
    }

    /// Sets the maximum time a connection can be idle before being retired.
    ///
    /// @param idleTimeout the idle timeout
    /// @return this builder
    public HikariDataSourceBuilder idleTimeout(Duration idleTimeout) {
        this.idleTimeout = Objects.requireNonNull(idleTimeout, "idleTimeout");
        return this;
    }

    /// Sets the maximum time to wait for a connection from the pool.
    ///
    /// @param connectionTimeout the connection timeout
    /// @return this builder
    public HikariDataSourceBuilder connectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = Objects.requireNonNull(connectionTimeout, "connectionTimeout");
        return this;
    }

    /// Sets the timeout for connection validation.
    ///
    /// @param validationTimeout the validation timeout
    /// @return this builder
    public HikariDataSourceBuilder validationTimeout(Duration validationTimeout) {
        this.validationTimeout = Objects.requireNonNull(validationTimeout, "validationTimeout");
        return this;
    }

    /// Enables or disables prepared statement caching.
    ///
    /// @param enabled true to enable caching
    /// @return this builder
    public HikariDataSourceBuilder cachePreparedStatements(boolean enabled) {
        this.cachePrepStmts = enabled;
        return this;
    }

    /// Sets the prepared statement cache size.
    ///
    /// @param size the cache size
    /// @return this builder
    public HikariDataSourceBuilder preparedStatementCacheSize(int size) {
        this.prepStmtCacheSize = size;
        return this;
    }

    /// Sets the maximum length of a prepared statement SQL that can be cached.
    ///
    /// @param limit the SQL length limit
    /// @return this builder
    public HikariDataSourceBuilder preparedStatementCacheSqlLimit(int limit) {
        this.prepStmtCacheSqlLimit = limit;
        return this;
    }

    /// Enables or disables auto-reconnect.
    ///
    /// @param enabled true to enable auto-reconnect
    /// @return this builder
    public HikariDataSourceBuilder autoReconnect(boolean enabled) {
        this.autoReconnect = enabled;
        return this;
    }

    /// Builds the HikariDataSource with the configured settings.
    ///
    /// @return a new HikariDataSource instance
    public HikariDataSource build() {
        HikariConfig config = new HikariConfig();

        // Basic connection settings
        config.setDriverClassName(DRIVER_CLASS);
        config.setJdbcUrl(credentials.jdbcUrl());
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());

        // Pool settings
        if (poolName != null) {
            config.setPoolName(poolName);
        }
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setMaxLifetime(maxLifetime.toMillis());
        config.setIdleTimeout(idleTimeout.toMillis());
        config.setConnectionTimeout(connectionTimeout.toMillis());
        config.setValidationTimeout(validationTimeout.toMillis());

        // MariaDB-specific optimizations
        config.addDataSourceProperty("cachePrepStmts", String.valueOf(cachePrepStmts));
        config.addDataSourceProperty("prepStmtCacheSize", String.valueOf(prepStmtCacheSize));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", String.valueOf(prepStmtCacheSqlLimit));
        config.addDataSourceProperty("autoReconnect", String.valueOf(autoReconnect));
        config.addDataSourceProperty("characterEncoding", "utf-8");

        // Add extra properties from credentials
        for (Map.Entry<String, String> entry : credentials.properties().entrySet()) {
            config.addDataSourceProperty(entry.getKey(), entry.getValue());
        }

        return new HikariDataSource(config);
    }
}
