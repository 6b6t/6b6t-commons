package net.blockhost.commons.database.mariadb;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.blockhost.commons.database.core.DatabaseCredentials;

import java.time.Duration;

/// ConfigLib-compatible MariaDB database configuration class.
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
///     private MariaDbConfig database = new MariaDbConfig();
///
///     public MariaDbConfig database() {
///         return database;
///     }
/// }
/// ```
///
/// To use with the database utilities, convert to [DatabaseCredentials]:
/// ```java
/// DatabaseCredentials credentials = config.database().toCredentials();
/// SQLManager sqlManager = SQLManager.builder()
///     .credentials(credentials)
///     .maxPoolSize(config.database().maxPoolSize())
///     .minIdle(config.database().minIdle())
///     .build();
/// ```
///
/// @see DatabaseCredentials
/// @see net.blockhost.commons.database.core.SQLManager
@Configuration
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class MariaDbConfig {

    /// MariaDB JDBC driver class name.
    public static final String DRIVER_CLASS = "org.mariadb.jdbc.Driver";

    @Comment("The database server hostname or IP address")
    private String host = "localhost";

    @Comment("The database server port")
    private int port = 3306;

    @Comment("The name of the database to connect to")
    private String database = "minecraft";

    @Comment("The username for database authentication")
    private String username = "root";

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
    /// [net.blockhost.commons.database.core.SQLManager] or [net.blockhost.commons.database.core.HikariDataSourceBuilder].
    ///
    /// @return a new DatabaseCredentials instance
    public DatabaseCredentials toCredentials() {
        return DatabaseCredentials.builder()
                .jdbcUrl(jdbcUrl())
                .driverClassName(DRIVER_CLASS)
                .username(username)
                .password(password)
                .connectionTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                .build();
    }

    /// Constructs the JDBC URL for MariaDB connections.
    ///
    /// @return the JDBC URL string
    public String jdbcUrl() {
        return "jdbc:mariadb://%s:%d/%s".formatted(host, port, database);
    }
}
