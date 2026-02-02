package net.blockhost.commons.database.core;

import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Generic SQL connection manager with HikariCP connection pooling.
///
/// This class provides a complete solution for managing database connections in plugins,
/// including connection pooling, configuration reloads, reconnection handling, and
/// table creation/migration support.
///
/// ## Features
/// - **Connection Pooling**: Uses HikariCP for efficient connection management
/// - **Config Reloads**: Supports hot-reloading database configuration
/// - **Auto-Reconnect**: Automatically attempts to reconnect on connection loss
/// - **Table Management**: Built-in support for table creation and migrations
/// - **Thread Safety**: All operations are thread-safe
///
/// ## Example Usage with PooledDatabaseConfig (Recommended)
/// ```java
/// // Using MariaDbConfig from commons-database-mariadb
/// MariaDbConfig dbConfig = config.database();
///
/// SQLManager sqlManager = SQLManager.create(dbConfig)
///     .poolName("MyPlugin-Pool")
///     .logger(plugin.getLogger())
///     .build();
///
/// // Register tables to create on connect
/// sqlManager.registerTable("""
///     CREATE TABLE IF NOT EXISTS players (
///         uuid VARCHAR(36) PRIMARY KEY,
///         name VARCHAR(16) NOT NULL
///     )
/// """);
///
/// // Connect and create tables
/// sqlManager.connect();
///
/// // Use connections safely with callbacks
/// sqlManager.withConnection(connection -> {
///     // Use connection safely
/// });
///
/// // Shutdown when done
/// sqlManager.shutdown();
/// ```
///
/// ## Using Withers for Config Overrides
/// ```java
/// // Create a modified config for a specific use case
/// MariaDbConfig authMeConfig = config.database()
///     .withDatabase("authme")
///     .withMaxPoolSize(5);
///
/// SQLManager authMeManager = SQLManager.create(authMeConfig)
///     .poolName("AuthMe-Pool")
///     .build();
/// ```
///
/// @see DatabaseCredentials
/// @see PooledDatabaseConfig
/// @see HikariDataSourceBuilder
public final class SQLManager {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<String> tableStatements = new ArrayList<>();
    private final List<Migration> migrations = new ArrayList<>();

    private volatile @Nullable HikariDataSource dataSource;
    private volatile DatabaseCredentials credentials;
    private volatile boolean connected;

    private final int maxPoolSize;
    private final int minIdle;
    private final @Nullable String poolName;
    private final @Nullable Logger logger;
    private final Consumer<String> infoLogger;
    private final Consumer<String> warningLogger;

    private SQLManager(Builder builder) {
        this.credentials = Objects.requireNonNull(builder.credentials, "credentials");
        this.maxPoolSize = builder.maxPoolSize;
        this.minIdle = builder.minIdle;
        this.poolName = builder.poolName;
        this.logger = builder.logger;
        this.infoLogger = builder.infoLogger != null
                ? builder.infoLogger
                : msg -> {
                    if (logger != null) {
                        logger.info(msg);
                    }
                };
        this.warningLogger = builder.warningLogger != null
                ? builder.warningLogger
                : msg -> {
                    if (logger != null) {
                        logger.warning(msg);
                    }
                };
    }

    /// Creates a new builder for SQLManager using a pooled database configuration.
    ///
    /// This is the recommended way to create an SQLManager. The configuration
    /// provides credentials, pool size, and idle connection settings.
    ///
    /// @param config the pooled database configuration
    /// @return a new builder instance with config applied
    public static Builder create(PooledDatabaseConfig config) {
        Objects.requireNonNull(config, "config");
        return new Builder()
                .credentials(config.toCredentials())
                .maxPoolSize(config.maxPoolSize())
                .minIdle(config.minIdle());
    }

    /// Connects to the database and initializes the connection pool.
    ///
    /// This method will create the HikariCP connection pool using the current
    /// configuration. If already connected, this method does nothing.
    ///
    /// After connecting, all registered table creation statements and migrations
    /// will be executed.
    public void connect() {
        lock.writeLock().lock();
        try {
            HikariDataSource ds = dataSource;
            if (ds != null && !ds.isClosed()) {
                return;
            }

            HikariDataSourceBuilder hikariBuilder = HikariDataSourceBuilder.create(credentials)
                    .maximumPoolSize(maxPoolSize)
                    .minimumIdle(minIdle);
            if (poolName != null) {
                hikariBuilder.poolName(poolName);
            }

            dataSource = hikariBuilder.build();
            connected = true;

            infoLogger.accept("Connected to database: " + credentials.jdbcUrl());

            // Create tables and run migrations
            createTables();
            runMigrations();

        } catch (Exception e) {
            connected = false;
            warningLogger.accept("Failed to connect to database: " + e.getMessage());
            if (logger != null) {
                logger.log(Level.SEVERE, "Database connection failed", e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /// Gets a connection from the pool.
    ///
    /// This method returns null instead of throwing an exception when unable
    /// to get a connection. This allows for easier error handling in calling code.
    ///
    /// **Important**: The returned connection must be closed after use.
    /// Use try-with-resources for automatic cleanup.
    ///
    /// @return a database connection, or null if unavailable
    public @Nullable Connection getConnection() {
        lock.readLock().lock();
        try {
            HikariDataSource ds = dataSource;
            if (ds == null || ds.isClosed()) {
                // Need write lock for reconnect - release read lock first
                lock.readLock().unlock();
                try {
                    reconnect();
                } finally {
                    lock.readLock().lock();
                }

                ds = dataSource;
                if (ds == null || ds.isClosed()) {
                    return null;
                }
            }

            return ds.getConnection();
        } catch (SQLException e) {
            warningLogger.accept("Failed to get database connection: " + e.getMessage());
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /// Executes an action with a database connection.
    ///
    /// This is a convenience method that handles connection retrieval and
    /// automatic cleanup. The connection is automatically closed after the
    /// action completes.
    ///
    /// @param action the action to execute with the connection
    /// @return true if the action executed successfully, false if no connection was available
    public boolean withConnection(ConnectionConsumer action) {
        Connection connection = getConnection();
        if (connection == null) {
            return false;
        }

        try (connection) {
            action.accept(connection);
            return true;
        } catch (SQLException e) {
            warningLogger.accept("Database operation failed: " + e.getMessage());
            return false;
        }
    }

    /// Executes an action with a database connection and returns a result.
    ///
    /// @param action the action to execute with the connection
    /// @param <T> the type of result
    /// @return the result, or null if no connection was available or an error occurred
    public <T> @Nullable T withConnectionResult(ConnectionFunction<T> action) {
        Connection connection = getConnection();
        if (connection == null) {
            return null;
        }

        try (connection) {
            return action.apply(connection);
        } catch (SQLException e) {
            warningLogger.accept("Database operation failed: " + e.getMessage());
            return null;
        }
    }

    /// Attempts to reconnect to the database.
    ///
    /// This method closes the existing connection pool (if any) and creates
    /// a new one with the current configuration.
    public void reconnect() {
        lock.writeLock().lock();
        try {
            closeDataSource();
            connected = false;
        } finally {
            lock.writeLock().unlock();
        }
        connect();
    }

    /// Reloads the database configuration and reconnects.
    ///
    /// This method allows hot-reloading of database configuration. The existing
    /// connection pool is closed and a new one is created with the new configuration.
    ///
    /// @param newCredentials the new database credentials
    public void reload(DatabaseCredentials newCredentials) {
        Objects.requireNonNull(newCredentials, "newCredentials");
        lock.writeLock().lock();
        try {
            this.credentials = newCredentials;
            closeDataSource();
            connected = false;
        } finally {
            lock.writeLock().unlock();
        }
        connect();
    }

    /// Registers a table creation SQL statement.
    ///
    /// These statements are executed when [#connect()] is called.
    /// Use `CREATE TABLE IF NOT EXISTS` to make statements idempotent.
    ///
    /// @param createTableSql the SQL statement to create the table
    public void registerTable(String createTableSql) {
        Objects.requireNonNull(createTableSql, "createTableSql");
        tableStatements.add(createTableSql);
    }

    /// Registers a database migration.
    ///
    /// Migrations are executed in order of their version number when [#connect()]
    /// is called. Each migration runs only once, tracked by a migrations table.
    ///
    /// @param version the migration version (must be unique and sequential)
    /// @param description a description of what the migration does
    /// @param sql the SQL statement(s) to execute
    public void registerMigration(int version, String description, String sql) {
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(sql, "sql");
        migrations.add(new Migration(version, description, sql));
    }

    /// Shuts down the connection pool.
    ///
    /// This method should be called when the plugin is disabled to properly
    /// release database resources.
    public void shutdown() {
        lock.writeLock().lock();
        try {
            closeDataSource();
            connected = false;
            infoLogger.accept("Database connection pool closed");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /// Checks if the manager is currently connected to the database.
    ///
    /// @return true if connected, false otherwise
    public boolean isConnected() {
        lock.readLock().lock();
        try {
            HikariDataSource ds = dataSource;
            return connected && ds != null && !ds.isClosed();
        } finally {
            lock.readLock().unlock();
        }
    }

    /// Gets the current database credentials.
    ///
    /// @return the current credentials
    public DatabaseCredentials getCredentials() {
        return credentials;
    }

    private void closeDataSource() {
        HikariDataSource ds = dataSource;
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
        dataSource = null;
    }

    private void createTables() {
        HikariDataSource ds = dataSource;
        if (tableStatements.isEmpty() || ds == null) {
            return;
        }

        try (Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()) {

            for (String sql : tableStatements) {
                stmt.executeUpdate(sql);
            }

            infoLogger.accept("Created " + tableStatements.size() + " database table(s)");

        } catch (SQLException e) {
            warningLogger.accept("Failed to create database tables: " + e.getMessage());
        }
    }

    private void runMigrations() {
        HikariDataSource ds = dataSource;
        if (migrations.isEmpty() || ds == null) {
            return;
        }

        try (Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()) {

            // Create migrations tracking table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS _migrations (
                    version INT PRIMARY KEY,
                    description VARCHAR(255) NOT NULL,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Get already applied migrations
            var appliedVersions = new HashSet<Integer>();
            try (var rs = stmt.executeQuery("SELECT version FROM _migrations")) {
                while (rs.next()) {
                    appliedVersions.add(rs.getInt("version"));
                }
            }

            // Apply pending migrations
            int applied = 0;
            for (Migration migration : migrations.stream()
                    .sorted(Comparator.comparingInt(Migration::version))
                    .toList()) {

                if (appliedVersions.contains(migration.version())) {
                    continue;
                }

                stmt.executeUpdate(migration.sql());

                try (var insertStmt =
                        conn.prepareStatement("INSERT INTO _migrations (version, description) VALUES (?, ?)")) {
                    insertStmt.setInt(1, migration.version());
                    insertStmt.setString(2, migration.description());
                    insertStmt.executeUpdate();
                }

                infoLogger.accept("Applied migration v" + migration.version() + ": " + migration.description());
                applied++;
            }

            if (applied > 0) {
                infoLogger.accept("Applied " + applied + " database migration(s)");
            }

        } catch (SQLException e) {
            warningLogger.accept("Failed to run database migrations: " + e.getMessage());
        }
    }

    /// Functional interface for connection consumers that may throw SQLException.
    @FunctionalInterface
    public interface ConnectionConsumer {
        /// Performs an operation using the given connection.
        ///
        /// @param connection the database connection
        /// @throws SQLException if a database error occurs
        void accept(Connection connection) throws SQLException;
    }

    /// Functional interface for connection functions that may throw SQLException.
    @FunctionalInterface
    public interface ConnectionFunction<T> {
        /// Performs an operation using the given connection and returns a result.
        ///
        /// @param connection the database connection
        /// @return the result
        /// @throws SQLException if a database error occurs
        T apply(Connection connection) throws SQLException;
    }

    private record Migration(int version, String description, String sql) {}

    /// Builder for creating SQLManager instances.
    @SuppressWarnings("NullAway.Init") // Builder pattern - fields initialized via setters before build()
    public static final class Builder {
        private @Nullable DatabaseCredentials credentials;
        private int maxPoolSize = 10;
        private int minIdle = 2;
        private @Nullable String poolName;
        private @Nullable Logger logger;
        private @Nullable Consumer<String> infoLogger;
        private @Nullable Consumer<String> warningLogger;

        private Builder() {}

        /// Sets all database settings from a pooled database configuration.
        ///
        /// This sets credentials, maxPoolSize, and minIdle from the config.
        /// Any previous values for these settings will be overwritten.
        ///
        /// @param config the pooled database configuration
        /// @return this builder
        public Builder config(PooledDatabaseConfig config) {
            Objects.requireNonNull(config, "config");
            this.credentials = config.toCredentials();
            this.maxPoolSize = config.maxPoolSize();
            this.minIdle = config.minIdle();
            return this;
        }

        private Builder credentials(DatabaseCredentials credentials) {
            this.credentials = Objects.requireNonNull(credentials, "credentials");
            return this;
        }

        private Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        private Builder minIdle(int minIdle) {
            this.minIdle = minIdle;
            return this;
        }

        /// Sets the connection pool name.
        ///
        /// This name will appear in thread names and JMX metrics.
        ///
        /// @param poolName the pool name
        /// @return this builder
        public Builder poolName(String poolName) {
            this.poolName = Objects.requireNonNull(poolName, "poolName");
            return this;
        }

        /// Sets the logger for database messages.
        ///
        /// @param logger the logger to use
        /// @return this builder
        public Builder logger(@Nullable Logger logger) {
            this.logger = logger;
            return this;
        }

        /// Sets a custom info log consumer.
        ///
        /// If set, this will be used instead of the logger for info messages.
        ///
        /// @param infoLogger the info log consumer
        /// @return this builder
        public Builder infoLogger(Consumer<String> infoLogger) {
            this.infoLogger = Objects.requireNonNull(infoLogger, "infoLogger");
            return this;
        }

        /// Sets a custom warning log consumer.
        ///
        /// If set, this will be used instead of the logger for warning messages.
        ///
        /// @param warningLogger the warning log consumer
        /// @return this builder
        public Builder warningLogger(Consumer<String> warningLogger) {
            this.warningLogger = Objects.requireNonNull(warningLogger, "warningLogger");
            return this;
        }

        /// Builds the SQLManager instance.
        ///
        /// @return a new SQLManager
        /// @throws NullPointerException if credentials is not set
        public SQLManager build() {
            return new SQLManager(this);
        }
    }
}
