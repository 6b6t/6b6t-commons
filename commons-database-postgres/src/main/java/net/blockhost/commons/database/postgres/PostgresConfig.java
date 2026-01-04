package net.blockhost.commons.database.postgres;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import net.blockhost.commons.database.core.DatabaseCredentials;
import net.blockhost.commons.database.core.PooledDatabaseConfig;

import java.time.Duration;

/// ConfigLib-compatible PostgreSQL database configuration class.
///
/// This class can be embedded in your plugin's configuration to provide
/// database connection settings. It uses ConfigLib annotations for YAML
/// serialization with helpful comments.
///
/// Example usage in a plugin configuration:
/// ```java
/// @Configuration
/// public class PluginConfig {
///     @Comment("Database connection settings")
///     private PostgresConfig database = new PostgresConfig();
///
///     public PostgresConfig database() {
///         return database;
///     }
/// }
/// ```
///
/// To use with SQLManager directly:
/// ```java
/// SQLManager sqlManager = SQLManager.create(config.database())
///     .poolName("MyPlugin-Pool")
///     .logger(logger)
///     .build();
/// ```
///
/// Use withers to create modified copies for specific use cases:
/// ```java
/// PostgresConfig analyticsConfig = config.database()
///     .withDatabase("analytics")
///     .withMaxPoolSize(5);
/// ```
///
/// @see DatabaseCredentials
/// @see net.blockhost.commons.database.core.SQLManager
@Configuration
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@With
public class PostgresConfig implements PooledDatabaseConfig {

    /// PostgreSQL JDBC driver class name.
    public static final String DRIVER_CLASS = "org.postgresql.Driver";

    @Comment("The database server hostname or IP address")
    private String host = "localhost";

    @Comment("The database server port")
    private int port = 5432;

    @Comment("The name of the database to connect to")
    private String database = "minecraft";

    @Comment("The username for database authentication")
    private String username = "postgres";

    @Comment("The password for database authentication")
    private String password = "";

    @Comment("Connection timeout in seconds")
    private int connectionTimeoutSeconds = 5;

    @Comment("Maximum number of connections in the pool (for HikariCP)")
    private int maxPoolSize = 10;

    @Comment("Minimum number of idle connections in the pool (for HikariCP)")
    private int minIdle = 2;

    /// Converts this configuration to a [DatabaseCredentials] instance.
    ///
    /// This is the recommended way to use the configuration with
    /// [net.blockhost.commons.database.core.SQLManager] or
    // [net.blockhost.commons.database.core.HikariDataSourceBuilder].
    ///
    /// @return a new DatabaseCredentials instance
    @Override
    public DatabaseCredentials toCredentials() {
        return DatabaseCredentials.builder()
                .jdbcUrl(jdbcUrl())
                .driverClassName(DRIVER_CLASS)
                .username(username)
                .password(password)
                .connectionTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                .build();
    }

    /// Constructs the JDBC URL for PostgreSQL connections.
    ///
    /// @return the JDBC URL string
    public String jdbcUrl() {
        return "jdbc:postgresql://%s:%d/%s".formatted(host, port, database);
    }
}
