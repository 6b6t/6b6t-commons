package net.blockhost.commons.database;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/// ConfigLib-compatible database configuration class.
///
/// This class can be embedded in your plugin's configuration to provide
/// database connection settings. It uses ConfigLib annotations for YAML
/// serialization with helpful comments.
///
/// Example usage in a plugin configuration:
/// <pre>
/// `class PluginConfig{settings")private DatabaseConfig database = new DatabaseConfig();public DatabaseConfig database(){return database;}}`</pre>
///
/// This will generate a YAML file like:
/// <pre>
/// `# Database connection settingsdatabase:# The database server hostname or IP addresshost: localhost# The database server portport: 3306# The name of the database to connect todatabase: minecraft# The username for database authenticationusername: root# The password for database authenticationpassword: ""# Connection timeout in secondsconnectionTimeoutSeconds: 5# Maximum number of connections in the pool (for HikariCP)maxPoolSize: 10# Minimum number of idle connections in the pool (for HikariCP)minIdle: 2`</pre>
///
/// To use with the database utilities, convert to [DatabaseCredentials]:
/// <pre>
/// `DatabaseCredentials credentials = config.database().toCredentials();try (Connection conn = MariaDbConnectionFactory.openConnection(credentials)){// use connection}// Or use with HikariCP:HikariDataSource dataSource = HikariDataSourceBuilder.create(config.database());`</pre>
///
/// @see DatabaseCredentials
/// @see MariaDbConnectionFactory
/// @see HikariDataSourceBuilder
@Configuration
public class DatabaseConfig {

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

    /// Creates a new DatabaseConfig with default values.
    public DatabaseConfig() {
        // Default constructor required by ConfigLib
    }

    /// Creates a new DatabaseConfig with specified values.
    ///
    /// @param host     the database host
    /// @param port     the database port
    /// @param database the database name
    /// @param username the username
    /// @param password the password
    public DatabaseConfig(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /// Converts this configuration to a [DatabaseCredentials] instance.
    ///
    /// This is the recommended way to use the configuration with
    /// [MariaDbConnectionFactory] or [HikariDataSourceBuilder].
    ///
    /// @return a new DatabaseCredentials instance
    public @NotNull DatabaseCredentials toCredentials() {
        return DatabaseCredentials.builder()
                .host(host)
                .port(port)
                .database(database)
                .username(username)
                .password(password)
                .connectionTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                .build();
    }

    /// Returns the database server hostname or IP address.
    ///
    /// @return the host
    public String host() {
        return host;
    }

    /// Returns the database server port.
    ///
    /// @return the port
    public int port() {
        return port;
    }

    /// Returns the name of the database to connect to.
    ///
    /// @return the database name
    public String database() {
        return database;
    }

    /// Returns the username for database authentication.
    ///
    /// @return the username
    public String username() {
        return username;
    }

    /// Returns the password for database authentication.
    ///
    /// @return the password
    public String password() {
        return password;
    }

    /// Returns the connection timeout in seconds.
    ///
    /// @return the connection timeout in seconds
    public int connectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    /// Returns the maximum pool size for HikariCP.
    ///
    /// @return the maximum pool size
    public int maxPoolSize() {
        return maxPoolSize;
    }

    /// Returns the minimum number of idle connections for HikariCP.
    ///
    /// @return the minimum idle connections
    public int minIdle() {
        return minIdle;
    }

    /// Constructs the JDBC URL for MariaDB connections.
    ///
    /// @return the JDBC URL string
    public String jdbcUrl() {
        return "jdbc:mariadb://%s:%d/%s".formatted(host, port, database);
    }
}
