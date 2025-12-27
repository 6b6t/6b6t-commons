package net.blockhost.commons.database;

import lombok.experimental.UtilityClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

/// Factory for creating MariaDB database connections.
///
/// This class provides static methods to create JDBC connections to MariaDB databases
/// using the provided [DatabaseCredentials]. It handles driver loading, connection
/// property configuration, and timeout settings.
///
/// Example usage:
/// ```java
/// DatabaseCredentials credentials = DatabaseCredentials.builder()
///     .host("localhost")
///     .database("mydb")
///     .username("user")
///     .password("pass")
///     .build();
///
/// try (Connection connection = MariaDbConnectionFactory.openConnection(credentials)) {
///     // Use the connection
/// }
/// ```
///
/// For connection pooling, consider using [HikariDataSourceBuilder] instead.
///
/// @see DatabaseCredentials
/// @see HikariDataSourceBuilder
@UtilityClass
public class MariaDbConnectionFactory {

    private static final String DRIVER_CLASS = "org.mariadb.jdbc.Driver";

    /// Opens a new database connection using the provided credentials.
    ///
    /// The connection will have timeout settings applied based on the credentials.
    public Connection openConnection(DatabaseCredentials credentials) throws SQLException {
        return openConnection(credentials, true);
    }

    /// Opens a new database connection with optional timeout settings.
    public Connection openConnection(DatabaseCredentials credentials, boolean applyTimeout) throws SQLException {
        ensureDriverLoaded();
        return DriverManager.getConnection(credentials.jdbcUrl(), buildConnectionProperties(credentials, applyTimeout));
    }

    /// Builds connection properties from the provided credentials.
    public Properties buildConnectionProperties(DatabaseCredentials credentials) {
        return buildConnectionProperties(credentials, true);
    }

    /// Builds connection properties with optional timeout settings.
    public Properties buildConnectionProperties(DatabaseCredentials credentials, boolean applyTimeout) {
        Properties properties = new Properties();
        properties.setProperty("user", credentials.username());
        properties.setProperty("password", credentials.password());

        if (applyTimeout) {
            Duration timeout = credentials.connectionTimeout();
            int timeoutMillis = (int) Math.max(1000, Math.min(timeout.toMillis(), Integer.MAX_VALUE));
            properties.setProperty("connectTimeout", String.valueOf(timeoutMillis));
            properties.setProperty("socketTimeout", String.valueOf(timeoutMillis));
        }

        // Enable batching for better performance with bulk operations
        properties.setProperty("rewriteBatchedStatements", "true");

        // Add any extra properties from the credentials
        for (Map.Entry<String, String> entry : credentials.properties().entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }

        return properties;
    }

    /// Ensures the MariaDB JDBC driver is loaded.
    public void ensureDriverLoaded() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS, true, MariaDbConnectionFactory.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB JDBC driver is not available on the classpath: " + DRIVER_CLASS, e);
        }
    }

    /// Validates that a connection can be established with the given credentials.
    ///
    /// This method attempts to open a connection, validates it, and then closes it.
    /// It's useful for verifying database connectivity during application startup.
    public void validateConnection(DatabaseCredentials credentials) throws SQLException {
        ensureDriverLoaded();

        Duration timeout = credentials.connectionTimeout();
        int timeoutSeconds = (int) Math.max(1, Math.min(timeout.toSeconds(), Integer.MAX_VALUE));

        DriverManager.setLoginTimeout(timeoutSeconds);

        try (Connection connection =
                DriverManager.getConnection(credentials.jdbcUrl(), buildConnectionProperties(credentials, true))) {
            if (!connection.isValid(timeoutSeconds)) {
                throw new SQLException("Connection is not valid within the timeout window");
            }
        }
    }
}
