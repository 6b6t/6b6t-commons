package net.blockhost.commons.database;

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
/// <pre>
/// `DatabaseCredentials credentials = DatabaseCredentials.builder().host("localhost").database("mydb").username("user").password("pass").build();try (Connection connection = MariaDbConnectionFactory.openConnection(credentials)){// Use the connection}`</pre>
///
/// For connection pooling, consider using [HikariDataSourceBuilder] instead.
///
/// @see DatabaseCredentials
/// @see HikariDataSourceBuilder
public final class MariaDbConnectionFactory {

    private static final String DRIVER_CLASS = "org.mariadb.jdbc.Driver";

    private MariaDbConnectionFactory() {
        // Utility class
    }

    /// Opens a new database connection using the provided credentials.
    ///
    /// The connection will have timeout settings applied based on the credentials.
    ///
    /// @param credentials the database credentials
    /// @return a new database connection
    /// @throws SQLException if a database access error occurs or the driver is not available
    public static Connection openConnection(DatabaseCredentials credentials) throws SQLException {
        return openConnection(credentials, true);
    }

    /// Opens a new database connection with optional timeout settings.
    ///
    /// @param credentials  the database credentials
    /// @param applyTimeout whether to apply timeout settings from the credentials
    /// @return a new database connection
    /// @throws SQLException if a database access error occurs or the driver is not available
    public static Connection openConnection(DatabaseCredentials credentials, boolean applyTimeout)
            throws SQLException {
        ensureDriverLoaded();
        return DriverManager.getConnection(credentials.jdbcUrl(), buildConnectionProperties(credentials, applyTimeout));
    }

    /// Builds connection properties from the provided credentials.
    ///
    /// @param credentials the database credentials
    /// @return a Properties object containing all connection properties
    public static Properties buildConnectionProperties(DatabaseCredentials credentials) {
        return buildConnectionProperties(credentials, true);
    }

    /// Builds connection properties with optional timeout settings.
    ///
    /// @param credentials  the database credentials
    /// @param applyTimeout whether to include timeout settings
    /// @return a Properties object containing connection properties
    public static Properties buildConnectionProperties(
            DatabaseCredentials credentials, boolean applyTimeout) {
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
    ///
    /// @throws SQLException if the driver class cannot be found
    public static void ensureDriverLoaded() throws SQLException {
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
    ///
    /// @param credentials the database credentials to validate
    /// @throws SQLException if the connection cannot be established or is invalid
    public static void validateConnection(DatabaseCredentials credentials) throws SQLException {
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
