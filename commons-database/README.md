# commons-database

MariaDB database utilities for 6b6t plugins, providing connection management with HikariCP connection pooling.

## Installation

```kotlin
dependencies {
    implementation("net.blockhost.commons:commons-database:1.0.0-SNAPSHOT")
}
```

## Features

- **HikariCP Integration**: High-performance connection pooling
- **MariaDB Optimized**: Pre-configured for MariaDB best practices
- **ConfigLib Support**: `DatabaseConfig` class for YAML configuration
- **Builder Pattern**: Fluent API for customizing connection pools

## Quick Start

### Using DatabaseConfig with ConfigLib

```java
@Configuration
public class PluginConfig {
    @Comment("Database connection settings")
    private DatabaseConfig database = new DatabaseConfig();

    public DatabaseConfig database() {
        return database;
    }
}

// Load config and create connection pool
PluginConfig config = ConfigLoader.loadOrCreate(configPath, PluginConfig.class);
HikariDataSource dataSource = HikariDataSourceBuilder.createDataSource(config.database());

// Use the connection
try (Connection conn = dataSource.getConnection()) {
    // Execute queries
}

// Shutdown on plugin disable
dataSource.close();
```

### Generated YAML

```yaml
database:
  # The database server hostname or IP address
  host: localhost
  # The database server port
  port: 3306
  # The database name
  database: minecraft
  # The database username
  username: root
  # The database password
  password: ''
  # Connection timeout in seconds
  connectionTimeoutSeconds: 5
  # Maximum number of connections in the pool
  maxPoolSize: 10
  # Minimum number of idle connections
  minIdle: 2
```

### Using DatabaseCredentials Directly

```java
DatabaseCredentials credentials = DatabaseCredentials.builder()
    .host("localhost")
    .port(3306)
    .database("minecraft")
    .username("user")
    .password("secret")
    .build();

HikariDataSource dataSource = HikariDataSourceBuilder.create(credentials)
    .poolName("MyPlugin-Pool")
    .maxPoolSize(15)
    .minIdle(3)
    .build();
```

### Custom Connection Pool Configuration

```java
HikariDataSource dataSource = HikariDataSourceBuilder.create(credentials)
    .poolName("MyPlugin-DB")
    .maxPoolSize(20)
    .minIdle(5)
    .connectionTimeout(Duration.ofSeconds(10))
    .idleTimeout(Duration.ofMinutes(10))
    .maxLifetime(Duration.ofMinutes(30))
    .build();
```

### Simple Connection (Without Pooling)

For scripts or simple use cases:

```java
DatabaseCredentials credentials = DatabaseCredentials.builder()
    .host("localhost")
    .database("minecraft")
    .username("user")
    .password("secret")
    .build();

// Single connection (remember to close it)
try (Connection conn = MariaDbConnectionFactory.openConnection(credentials)) {
    // Execute queries
}

// Validate connectivity
boolean valid = MariaDbConnectionFactory.validateConnection(credentials);
```

## SQLManager - High-Level Connection Management

`SQLManager` provides a complete solution for managing database connections with connection pooling, config reloads, reconnection handling, and table/migration support:

```java
// Create manager with configuration
SQLManager sqlManager = SQLManager.builder()
    .config(databaseConfig)
    .poolName("MyPlugin-Pool")
    .logger(plugin.getLogger())
    .build();

// Register tables to create on connect
sqlManager.registerTable("""
    CREATE TABLE IF NOT EXISTS players (
        uuid VARCHAR(36) PRIMARY KEY,
        name VARCHAR(16) NOT NULL,
        last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
""");

// Register database migrations
sqlManager.registerMigration(1, "Add email column",
    "ALTER TABLE players ADD COLUMN email VARCHAR(255)");
sqlManager.registerMigration(2, "Add index on name",
    "CREATE INDEX idx_players_name ON players(name)");

// Connect and create tables/run migrations
sqlManager.connect();
```

### Using Connections

```java
// Get connection (returns null on error, no exception thrown)
Connection conn = sqlManager.getConnection();
if (conn != null) {
    try (conn) {
        // Use connection
    }
}

// Or use callback pattern (recommended)
sqlManager.withConnection(conn -> {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players")) {
        ResultSet rs = stmt.executeQuery();
        // Process results
    }
});

// With result
String name = sqlManager.withConnectionResult(conn -> {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT name FROM players WHERE uuid = ?")) {
        stmt.setString(1, uuid);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getString("name") : null;
    }
});
```

### Configuration Reloading

```java
// Hot-reload database configuration
sqlManager.reload(newDatabaseConfig);

// Manual reconnect
sqlManager.reconnect();

// Check connection status
if (sqlManager.isConnected()) {
    // Database is available
}
```

### Lifecycle Management

```java
// In your plugin's onEnable
sqlManager.connect();

// In your plugin's onDisable
sqlManager.shutdown();
```

## Classes

### DatabaseCredentials

Immutable holder for database connection parameters.

```java
DatabaseCredentials credentials = DatabaseCredentials.builder()
    .host("db.example.com")
    .port(3306)
    .database("mydb")
    .username("user")
    .password("pass")
    .connectionTimeout(5)
    .property("useSSL", "true")
    .build();

String jdbcUrl = credentials.jdbcUrl();
// jdbc:mariadb://db.example.com:3306/mydb?useSSL=true
```

### DatabaseConfig

ConfigLib-compatible configuration class with sensible defaults.

| Field | Default | Description |
|-------|---------|-------------|
| `host` | `localhost` | Database server hostname |
| `port` | `3306` | Database server port |
| `database` | `minecraft` | Database name |
| `username` | `root` | Database username |
| `password` | (empty) | Database password |
| `connectionTimeoutSeconds` | `5` | Connection timeout |
| `maxPoolSize` | `10` | Maximum pool connections |
| `minIdle` | `2` | Minimum idle connections |

### HikariDataSourceBuilder

Fluent builder for HikariCP connection pools with MariaDB optimizations.

**Default Configuration:**
- `cachePrepStmts=true`
- `prepStmtCacheSize=250`
- `prepStmtCacheSqlLimit=2048`
- `useServerPrepStmts=true`
- `rewriteBatchedStatements=true`
- `useLocalSessionState=true`
- `cacheResultSetMetadata=true`
- UTF-8 character encoding

### MariaDbConnectionFactory

Low-level factory for creating individual MariaDB connections.

```java
// Ensure driver is loaded
MariaDbConnectionFactory.ensureDriverLoaded();

// Open a single connection
Connection conn = MariaDbConnectionFactory.openConnection(credentials);

// Validate connection parameters
boolean isValid = MariaDbConnectionFactory.validateConnection(credentials);
```

### SQLManager

High-level connection manager with full lifecycle support.

| Feature | Description |
|---------|-------------|
| Connection Pooling | Uses HikariCP internally |
| Config Reloads | Hot-reload with `reload(newConfig)` |
| Auto-Reconnect | Attempts reconnection on connection loss |
| Table Creation | `registerTable(sql)` runs on connect |
| DB Migrations | `registerMigration(version, desc, sql)` with tracking |
| Thread Safety | All operations are thread-safe |

## Best Practices

1. **Always close connections**: Use try-with-resources
2. **Pool for plugins**: Use HikariCP for long-running plugins
3. **Configure pool size**: Match your concurrent query needs
4. **Handle exceptions**: Wrap in appropriate error handling

```java
public class DatabaseManager {
    private final HikariDataSource dataSource;
    
    public DatabaseManager(DatabaseConfig config) {
        this.dataSource = HikariDataSourceBuilder.createDataSource(config);
    }
    
    public void executeQuery(String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }
    
    public void shutdown() {
        dataSource.close();
    }
}
```

## API Documentation

Full Javadoc available at: **https://6b6t.github.io/6b6t-commons/**

## Related Modules

- [commons-config](../commons-config) - For loading DatabaseConfig from YAML
