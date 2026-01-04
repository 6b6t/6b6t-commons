package net.blockhost.commons.database.core;

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
/// This class encapsulates all the information needed to connect to a SQL database,
/// including JDBC URL, driver class, credentials, timeout settings, and additional
/// connection properties. It is dialect-independent - the specific database modules
/// (mariadb, postgres) provide the JDBC URL and driver class.
///
/// Use the builder to create instances:
/// ```java
/// DatabaseCredentials credentials = DatabaseCredentials.builder()
///     .jdbcUrl("jdbc:mariadb://localhost:3306/mydb")
///     .driverClassName("org.mariadb.jdbc.Driver")
///     .username("user")
///     .password("pass")
///     .build();
/// ```
///
/// @see HikariDataSourceBuilder
@Getter
@Accessors(fluent = true)
@Builder(toBuilder = true)
@SuppressWarnings("NullAway.Init") // Lombok @Builder handles field initialization
public final class DatabaseCredentials {

    private final String jdbcUrl;
    private final String driverClassName;
    private final String username;

    @Builder.Default
    private final String password = "";

    @Builder.Default
    private final Duration connectionTimeout = Duration.ofSeconds(5);

    @Singular
    private final Map<String, String> properties;

    /// Custom builder with validation.
    public static class DatabaseCredentialsBuilder {

        /// Sets the JDBC URL for the database connection.
        public DatabaseCredentialsBuilder jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl").trim();
            return this;
        }

        /// Sets the JDBC driver class name.
        public DatabaseCredentialsBuilder driverClassName(String driverClassName) {
            this.driverClassName =
                    Objects.requireNonNull(driverClassName, "driverClassName").trim();
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
