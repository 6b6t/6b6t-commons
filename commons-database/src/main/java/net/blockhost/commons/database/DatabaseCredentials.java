package net.blockhost.commons.database;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/// Immutable holder for database connection credentials and configuration.
///
/// This class encapsulates all the information needed to connect to a MariaDB database,
/// including host, port, database name, credentials, timeout settings, and additional
/// connection properties.
///
/// Use the [Builder] to create instances:
/// ```java
/// DatabaseCredentials credentials = DatabaseCredentials.builder()
///     .host("localhost")
///     .port(3306)
///     .database("mydb")
///     .username("user")
///     .password("pass")
///     .build();
/// ```
///
/// @see MariaDbConnectionFactory
/// @see HikariDataSourceBuilder
@Getter
@Accessors(fluent = true)
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

    /// Creates a new builder for [DatabaseCredentials].
    ///
    /// @return a new builder instance
    public static Builder builder() {
        return new Builder();
    }

    /// Constructs the JDBC URL for MariaDB connections.
    ///
    /// @return the JDBC URL string
    public String jdbcUrl() {
        return "jdbc:mariadb://%s:%d/%s".formatted(host, port, database);
    }

    /// Builder for creating [DatabaseCredentials] instances.
    public static final class Builder {

        private String host;
        private int port = 3306;
        private String database;
        private String username;
        private String password;
        private Duration connectionTimeout = Duration.ofSeconds(5);
        private final Map<String, String> properties = new LinkedHashMap<>();

        private Builder() {}

        /// Sets the database host address.
        public Builder host(String host) {
            this.host = Objects.requireNonNull(host, "host").trim();
            return this;
        }

        /// Sets the database port.
        public Builder port(int port) {
            if (port <= 0) {
                throw new IllegalArgumentException("Port must be positive: " + port);
            }
            this.port = port;
            return this;
        }

        /// Sets the database name.
        public Builder database(String database) {
            this.database = Objects.requireNonNull(database, "database").trim();
            return this;
        }

        /// Sets the username for authentication.
        public Builder username(String username) {
            this.username = Objects.requireNonNull(username, "username").trim();
            return this;
        }

        /// Sets the password for authentication.
        public Builder password(@Nullable String password) {
            this.password = password;
            return this;
        }

        /// Sets the connection timeout duration.
        public Builder connectionTimeout(Duration timeout) {
            Objects.requireNonNull(timeout, "timeout");
            if (timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException("Connection timeout must be positive: " + timeout);
            }
            this.connectionTimeout = timeout;
            return this;
        }

        /// Sets the connection timeout in seconds.
        public Builder connectionTimeoutSeconds(int seconds) {
            return connectionTimeout(Duration.ofSeconds(seconds));
        }

        /// Adds an additional connection property.
        public Builder property(String key, String value) {
            this.properties.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /// Adds multiple connection properties.
        public Builder properties(Map<String, String> properties) {
            Objects.requireNonNull(properties, "properties");
            this.properties.putAll(properties);
            return this;
        }

        /// Builds the [DatabaseCredentials] instance.
        public DatabaseCredentials build() {
            return new DatabaseCredentials(this);
        }
    }
}
