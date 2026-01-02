package net.blockhost.commons.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// Immutable holder for database connection credentials and configuration.
///
/// This class encapsulates all the information needed to connect to a MariaDB database,
/// including host, port, database name, credentials, timeout settings, and additional
/// connection properties.
///
/// Use the builder to create instances:
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
@Builder(toBuilder = true)
@SuppressWarnings("NullAway.Init") // Lombok @Builder handles field initialization
public final class DatabaseCredentials {

    private final String host;

    @Builder.Default
    private final int port = 3306;

    private final String database;
    private final String username;

    @Builder.Default
    private final String password = "";

    @Builder.Default
    private final Duration connectionTimeout = Duration.ofSeconds(5);

    @Singular
    private final Map<String, String> properties;

    /// Constructs the JDBC URL for MariaDB connections.
    ///
    /// @return the JDBC URL string
    public String jdbcUrl() {
        return "jdbc:mariadb://%s:%d/%s".formatted(host, port, database);
    }

    /// Custom builder with validation.
    public static class DatabaseCredentialsBuilder {

        /// Sets the database host address.
        public DatabaseCredentialsBuilder host(String host) {
            this.host = Objects.requireNonNull(host, "host").trim();
            return this;
        }

        /// Sets the database port.
        public DatabaseCredentialsBuilder port(int port) {
            if (port <= 0) {
                throw new IllegalArgumentException("Port must be positive: " + port);
            }
            this.port$value = port;
            this.port$set = true;
            return this;
        }

        /// Sets the database name.
        public DatabaseCredentialsBuilder database(String database) {
            this.database = Objects.requireNonNull(database, "database").trim();
            return this;
        }

        /// Sets the username for authentication.
        public DatabaseCredentialsBuilder username(String username) {
            this.username = Objects.requireNonNull(username, "username").trim();
            return this;
        }

        /// Sets the password for authentication.
        public DatabaseCredentialsBuilder password(@Nullable String password) {
            this.password$value = password != null ? password : "";
            this.password$set = true;
            return this;
        }

        /// Sets the connection timeout duration.
        public DatabaseCredentialsBuilder connectionTimeout(Duration timeout) {
            Objects.requireNonNull(timeout, "timeout");
            if (timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException("Connection timeout must be positive: " + timeout);
            }
            this.connectionTimeout$value = timeout;
            this.connectionTimeout$set = true;
            return this;
        }

        /// Sets the connection timeout in seconds.
        public DatabaseCredentialsBuilder connectionTimeoutSeconds(int seconds) {
            return connectionTimeout(Duration.ofSeconds(seconds));
        }
    }
}
