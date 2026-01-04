package net.blockhost.commons.database.redis;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

/// ConfigLib-compatible Redis configuration class.
///
/// This class can be embedded in your plugin's configuration to provide
/// Redis connection settings. It uses ConfigLib annotations for YAML
/// serialization with helpful comments.
///
/// Example usage in a plugin configuration:
/// ```java
/// @Configuration
/// public class PluginConfig {
///     @Comment("Redis connection settings")
///     private RedisConfig redis = new RedisConfig();
///
///     public RedisConfig redis() {
///         return redis;
///     }
/// }
/// ```
///
/// @see RedisManager
@Configuration
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@With
public class RedisConfig {

    @Comment("The Redis server hostname or IP address")
    private String host = "localhost";

    @Comment("The Redis server port")
    private int port = 6379;

    @Comment("The password for Redis authentication (leave empty for no auth)")
    private String password = "";

    @Comment("The Redis database index (0-15)")
    private int database = 0;

    @Comment("Connection timeout in milliseconds")
    private int connectionTimeoutMs = 2000;

    @Comment("Socket timeout in milliseconds")
    private int socketTimeoutMs = 2000;

    @Comment("Maximum number of connections in the pool")
    private int maxPoolSize = 8;

    @Comment("Maximum number of idle connections in the pool")
    private int maxIdle = 8;

    @Comment("Minimum number of idle connections in the pool")
    private int minIdle = 0;

    @Comment("Enable SSL/TLS connection")
    private boolean ssl = false;

    /// Checks if authentication is required.
    ///
    /// @return true if a password is set
    public boolean requiresAuth() {
        return password != null && !password.isEmpty();
    }
}
