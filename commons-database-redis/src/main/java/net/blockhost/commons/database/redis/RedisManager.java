package net.blockhost.commons.database.redis;

import org.jspecify.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Redis connection manager with Jedis connection pooling.
///
/// This class provides a complete solution for managing Redis connections in plugins,
/// including connection pooling, configuration reloads, and reconnection handling.
///
/// ## Features
/// - **Connection Pooling**: Uses JedisPool for efficient connection management
/// - **Config Reloads**: Supports hot-reloading Redis configuration
/// - **Auto-Reconnect**: Automatically attempts to reconnect on connection loss
/// - **Thread Safety**: All operations are thread-safe
///
/// ## Example Usage
/// ```java
/// RedisManager redisManager = RedisManager.builder()
///     .config(redisConfig)
///     .logger(plugin.getLogger())
///     .build();
///
/// // Connect to Redis
/// redisManager.connect();
///
/// // Use connections with callback pattern
/// redisManager.withConnection(jedis -> {
///     jedis.set("key", "value");
/// });
///
/// // Get values
/// String value = redisManager.withConnectionResult(jedis -> jedis.get("key"));
///
/// // Reload configuration
/// redisManager.reload(newConfig);
///
/// // Shutdown when done
/// redisManager.shutdown();
/// ```
///
/// @see RedisConfig
public final class RedisManager {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile @Nullable JedisPool pool;
    private volatile RedisConfig config;
    private volatile boolean connected;

    private final @Nullable Logger logger;
    private final Consumer<String> infoLogger;
    private final Consumer<String> warningLogger;

    private RedisManager(Builder builder) {
        this.config = Objects.requireNonNull(builder.config, "config");
        this.logger = builder.logger;
        this.infoLogger = builder.infoLogger != null
                ? builder.infoLogger
                : msg -> {
                    if (logger != null) logger.info(msg);
                };
        this.warningLogger = builder.warningLogger != null
                ? builder.warningLogger
                : msg -> {
                    if (logger != null) logger.warning(msg);
                };
    }

    /// Creates a new builder for RedisManager.
    ///
    /// @return a new builder instance
    public static Builder builder() {
        return new Builder();
    }

    /// Connects to Redis and initializes the connection pool.
    ///
    /// This method will create the Jedis connection pool using the current
    /// configuration. If already connected, this method does nothing.
    public void connect() {
        lock.writeLock().lock();
        try {
            if (pool != null && !pool.isClosed()) {
                return;
            }

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(config.maxPoolSize());
            poolConfig.setMaxIdle(config.maxIdle());
            poolConfig.setMinIdle(config.minIdle());
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            if (config.requiresAuth()) {
                pool = new JedisPool(
                        poolConfig,
                        config.host(),
                        config.port(),
                        config.connectionTimeoutMs(),
                        config.socketTimeoutMs(),
                        config.password(),
                        config.database(),
                        null,
                        config.ssl());
            } else {
                pool = new JedisPool(
                        poolConfig,
                        config.host(),
                        config.port(),
                        config.connectionTimeoutMs(),
                        config.socketTimeoutMs(),
                        null,
                        config.database(),
                        null,
                        config.ssl());
            }

            // Test connection
            try (Jedis jedis = pool.getResource()) {
                jedis.ping();
            }

            connected = true;
            infoLogger.accept("Connected to Redis: " + config.host() + ":" + config.port());

        } catch (Exception e) {
            connected = false;
            warningLogger.accept("Failed to connect to Redis: " + e.getMessage());
            if (logger != null) {
                logger.log(Level.SEVERE, "Redis connection failed", e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /// Gets a Jedis connection from the pool.
    ///
    /// This method returns null instead of throwing an exception when unable
    /// to get a connection. This allows for easier error handling in calling code.
    ///
    /// **Important**: The returned connection must be closed after use.
    /// Use try-with-resources for automatic cleanup.
    ///
    /// @return a Jedis connection, or null if unavailable
    public @Nullable Jedis getConnection() {
        lock.readLock().lock();
        try {
            JedisPool p = pool;
            if (p == null || p.isClosed()) {
                // Need write lock for reconnect - release read lock first
                lock.readLock().unlock();
                try {
                    reconnect();
                } finally {
                    lock.readLock().lock();
                }

                p = pool;
                if (p == null || p.isClosed()) {
                    return null;
                }
            }

            return p.getResource();
        } catch (Exception e) {
            warningLogger.accept("Failed to get Redis connection: " + e.getMessage());
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /// Executes an action with a Redis connection.
    ///
    /// This is a convenience method that handles connection retrieval and
    /// automatic cleanup. The connection is automatically closed after the
    /// action completes.
    ///
    /// @param action the action to execute with the connection
    /// @return true if the action executed successfully, false if no connection was available
    public boolean withConnection(Consumer<Jedis> action) {
        Jedis jedis = getConnection();
        if (jedis == null) {
            return false;
        }

        try (jedis) {
            action.accept(jedis);
            return true;
        } catch (Exception e) {
            warningLogger.accept("Redis operation failed: " + e.getMessage());
            return false;
        }
    }

    /// Executes an action with a Redis connection and returns a result.
    ///
    /// @param action the action to execute with the connection
    /// @param <T> the type of result
    /// @return the result, or null if no connection was available or an error occurred
    public <T> @Nullable T withConnectionResult(Function<Jedis, T> action) {
        Jedis jedis = getConnection();
        if (jedis == null) {
            return null;
        }

        try (jedis) {
            return action.apply(jedis);
        } catch (Exception e) {
            warningLogger.accept("Redis operation failed: " + e.getMessage());
            return null;
        }
    }

    /// Attempts to reconnect to Redis.
    ///
    /// This method closes the existing connection pool (if any) and creates
    /// a new one with the current configuration.
    public void reconnect() {
        lock.writeLock().lock();
        try {
            closePool();
            connected = false;
        } finally {
            lock.writeLock().unlock();
        }
        connect();
    }

    /// Reloads the Redis configuration and reconnects.
    ///
    /// This method allows hot-reloading of Redis configuration. The existing
    /// connection pool is closed and a new one is created with the new configuration.
    ///
    /// @param newConfig the new Redis configuration
    public void reload(RedisConfig newConfig) {
        Objects.requireNonNull(newConfig, "newConfig");
        lock.writeLock().lock();
        try {
            this.config = newConfig;
            closePool();
            connected = false;
        } finally {
            lock.writeLock().unlock();
        }
        connect();
    }

    /// Shuts down the connection pool.
    ///
    /// This method should be called when the plugin is disabled to properly
    /// release Redis resources.
    public void shutdown() {
        lock.writeLock().lock();
        try {
            closePool();
            connected = false;
            infoLogger.accept("Redis connection pool closed");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /// Checks if the manager is currently connected to Redis.
    ///
    /// @return true if connected, false otherwise
    public boolean isConnected() {
        lock.readLock().lock();
        try {
            JedisPool p = pool;
            return connected && p != null && !p.isClosed();
        } finally {
            lock.readLock().unlock();
        }
    }

    /// Gets the current Redis configuration.
    ///
    /// @return the current configuration
    public RedisConfig getConfig() {
        return config;
    }

    private void closePool() {
        JedisPool p = pool;
        if (p != null && !p.isClosed()) {
            p.close();
        }
        pool = null;
    }

    /// Builder for creating RedisManager instances.
    @SuppressWarnings("NullAway.Init") // Builder pattern - fields initialized via setters before build()
    public static final class Builder {
        private @Nullable RedisConfig config;
        private @Nullable Logger logger;
        private @Nullable Consumer<String> infoLogger;
        private @Nullable Consumer<String> warningLogger;

        private Builder() {}

        /// Sets the Redis configuration.
        ///
        /// @param config the Redis configuration
        /// @return this builder
        public Builder config(RedisConfig config) {
            this.config = Objects.requireNonNull(config, "config");
            return this;
        }

        /// Sets the logger for Redis messages.
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

        /// Builds the RedisManager instance.
        ///
        /// @return a new RedisManager
        /// @throws NullPointerException if config is not set
        public RedisManager build() {
            return new RedisManager(this);
        }
    }
}
