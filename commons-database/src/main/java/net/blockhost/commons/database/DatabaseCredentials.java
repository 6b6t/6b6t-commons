package net.blockhost.commons.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable holder for database connection credentials and configuration.
 *
 * <p>This class encapsulates all the information needed to connect to a MariaDB database,
 * including host, port, database name, credentials, timeout settings, and additional
 * connection properties.
 *
 * <p>Use the {@link Builder} to create instances:
 * <pre>{@code
 * DatabaseCredentials credentials = DatabaseCredentials.builder()
 *     .host("localhost")
 *     .port(3306)
 *     .database("mydb")
 *     .username("user")
 *     .password("pass")
 *     .build();
 * }</pre>
 *
 * @see MariaDbConnectionFactory
 * @see HikariDataSourceBuilder
 */
public final class DatabaseCredentials {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final Duration connectionTimeout;
    private final Map<String, String> properties;

    private DatabaseCredentials(Builder builder) {
        this.host = Objects.requireNonNull(builder.host, "host");
        this.port = builder.port;
        this.database = Objects.requireNonNull(builder.database, "database");
        this.username = Objects.requireNonNull(builder.username, "username");
        this.password = builder.password != null ? builder.password : "";
        this.connectionTimeout = builder.connectionTimeout;
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(builder.properties));
    }

    /**
     * Creates a new builder for {@link DatabaseCredentials}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the database host address.
     *
     * @return the host address
     */
    public @NotNull String host() {
        return host;
    }

    /**
     * Returns the database port.
     *
     * @return the port number
     */
    public int port() {
        return port;
    }

    /**
     * Returns the database name.
     *
     * @return the database name
     */
    public @NotNull String database() {
        return database;
    }

    /**
     * Returns the username for authentication.
     *
     * @return the username
     */
    public @NotNull String username() {
        return username;
    }

    /**
     * Returns the password for authentication.
     *
     * @return the password, never null but may be empty
     */
    public @NotNull String password() {
        return password;
    }

    /**
     * Returns the connection timeout duration.
     *
     * @return the connection timeout
     */
    public @NotNull Duration connectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Returns additional connection properties.
     *
     * @return an unmodifiable map of additional properties
     */
    public @NotNull Map<String, String> properties() {
        return properties;
    }

    /**
     * Constructs the JDBC URL for MariaDB connections.
     *
     * @return the JDBC URL string
     */
    public @NotNull String jdbcUrl() {
        return String.format("jdbc:mariadb://%s:%d/%s", host, port, database);
    }

    /**
     * Builder for creating {@link DatabaseCredentials} instances.
     */
    public static final class Builder {
        private String host;
        private int port = 3306;
        private String database;
        private String username;
        private String password;
        private Duration connectionTimeout = Duration.ofSeconds(5);
        private final Map<String, String> properties = new LinkedHashMap<>();

        private Builder() {}

        /**
         * Sets the database host address.
         *
         * @param host the host address
         * @return this builder
         */
        public Builder host(@NotNull String host) {
            this.host = Objects.requireNonNull(host, "host").trim();
            return this;
        }

        /**
         * Sets the database port.
         *
         * @param port the port number (must be positive)
         * @return this builder
         * @throws IllegalArgumentException if port is not positive
         */
        public Builder port(int port) {
            if (port <= 0) {
                throw new IllegalArgumentException("Port must be positive: " + port);
            }
            this.port = port;
            return this;
        }

        /**
         * Sets the database name.
         *
         * @param database the database name
         * @return this builder
         */
        public Builder database(@NotNull String database) {
            this.database = Objects.requireNonNull(database, "database").trim();
            return this;
        }

        /**
         * Sets the username for authentication.
         *
         * @param username the username
         * @return this builder
         */
        public Builder username(@NotNull String username) {
            this.username = Objects.requireNonNull(username, "username").trim();
            return this;
        }

        /**
         * Sets the password for authentication.
         *
         * @param password the password, may be null
         * @return this builder
         */
        public Builder password(@Nullable String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the connection timeout duration.
         *
         * @param timeout the timeout duration (must be positive)
         * @return this builder
         * @throws IllegalArgumentException if timeout is negative or zero
         */
        public Builder connectionTimeout(@NotNull Duration timeout) {
            Objects.requireNonNull(timeout, "timeout");
            if (timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException("Connection timeout must be positive: " + timeout);
            }
            this.connectionTimeout = timeout;
            return this;
        }

        /**
         * Sets the connection timeout in seconds.
         *
         * @param seconds the timeout in seconds (must be positive)
         * @return this builder
         */
        public Builder connectionTimeoutSeconds(int seconds) {
            return connectionTimeout(Duration.ofSeconds(seconds));
        }

        /**
         * Adds an additional connection property.
         *
         * @param key   the property key
         * @param value the property value
         * @return this builder
         */
        public Builder property(@NotNull String key, @NotNull String value) {
            this.properties.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /**
         * Adds multiple connection properties.
         *
         * @param properties the properties to add
         * @return this builder
         */
        public Builder properties(@NotNull Map<String, String> properties) {
            Objects.requireNonNull(properties, "properties");
            this.properties.putAll(properties);
            return this;
        }

        /**
         * Builds the {@link DatabaseCredentials} instance.
         *
         * @return a new DatabaseCredentials instance
         * @throws NullPointerException if required fields are not set
         */
        public DatabaseCredentials build() {
            return new DatabaseCredentials(this);
        }
    }
}
