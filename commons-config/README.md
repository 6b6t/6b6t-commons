# commons-config

YAML configuration utilities for 6b6t plugins, built on [ConfigLib](https://github.com/Exlll/ConfigLib) with a complete migration framework for versioned configurations.

## Installation

```kotlin
dependencies {
    implementation("net.blockhost.commons:commons-config:1.0.0-SNAPSHOT")
}
```

## Features

- **Simple API**: Easy loading/saving of YAML configurations via `ConfigLoader`
- **ConfigLib Integration**: Full support for `@Configuration`, `@Comment`, and other annotations
- **Version Tracking**: `VersionAwareConfiguration` base class for versioned configs
- **Migration Framework**: Complete suite for migrating configs between versions
- **Raw YAML Access**: `RawYamlLoader` for low-level YAML manipulation with SnakeYAML

## Quick Start

### Basic Configuration

```java
@Configuration
public class MyPluginConfig {
    @Comment("The server hostname")
    private String host = "localhost";

    @Comment("The server port")
    private int port = 25565;

    @Comment("Enable debug mode")
    private boolean debug = false;

    // Getters...
}

// Load or create configuration
Path configPath = plugin.getDataFolder().toPath().resolve("config.yml");
MyPluginConfig config = ConfigLoader.loadOrCreate(configPath, MyPluginConfig.class);
```

### Versioned Configuration with Migrations

For configurations that evolve over time:

```java
@Configuration
public class MyPluginConfig extends VersionAwareConfiguration {
    private static final int CURRENT_VERSION = 3;

    private String hostname = "localhost";  // Renamed from 'host' in v2
    private int port = 25565;
    private int timeout = 30;               // Added in v2

    public MyPluginConfig() {
        super(CURRENT_VERSION);
    }
}
```

Define migrations:

```java
ConfigMigrator migrator = ConfigMigrator.builder()
    .createBackups(true)  // Backup before migrating
    .register(Migration.of(2, "Add timeout and rename host", ctx -> {
        ctx.setDefault("timeout", 30);
        ctx.rename("host", "hostname");
    }))
    .register(Migration.of(3, "Add new feature flags", ctx -> {
        ctx.getOrCreateNestedMap("features").put("newFeature", true);
    }))
    .beforeMigration(m -> logger.info("Applying: " + m.description()))
    .build();

// Load with automatic migration
MyPluginConfig config = migrator.migrateAndLoad(
    configPath, 
    MyPluginConfig.class, 
    MyPluginConfig.CURRENT_VERSION
);
```

## Configuration Holder Pattern

For dependency injection and config reloading support, use `ConfigurationHolder`:

```java
// Create a plugin-specific holder
public class MyPluginConfigHolder extends ConfigurationHolder<MyPluginConfig> {
    public MyPluginConfigHolder(Path configPath) {
        super(() -> ConfigLoader.loadOrCreate(configPath, MyPluginConfig.class));
    }
}

// Or with migration support
public class MyPluginConfigHolder extends ConfigurationHolder<MyPluginConfig> {
    public MyPluginConfigHolder(Path configPath, ConfigMigrator migrator) {
        super(() -> migrator.migrateAndLoad(configPath, MyPluginConfig.class, 3));
    }
}
```

Usage in services:

```java
public class MyService {
    private final MyPluginConfigHolder configHolder;

    public MyService(MyPluginConfigHolder configHolder) {
        this.configHolder = configHolder;
    }

    public void doSomething() {
        MyPluginConfig config = configHolder.get();  // Always gets latest config
        // Use config...
    }
}

// Reload config at runtime
configHolder.reload();

// Add reload callback
configHolder.onReload(newConfig -> {
    logger.info("Config reloaded!");
});
```

**Thread Safety:** `ConfigurationHolder` uses `AtomicReference` internally, making it safe to use across threads.

## Migration Framework

### Core Components

| Component | Description |
|-----------|-------------|
| `Migration` | Defines a single migration step between versions |
| `MigrationContext` | Provides data access and utility methods during migration |
| `MigrationRegistry` | Stores and manages registered migrations |
| `MigrationExecutor` | Executes migrations sequentially with callbacks |
| `ConfigMigrator` | High-level API combining all components |
| `RawYamlLoader` | Low-level YAML loading/saving with SnakeYAML |

### Implementing Migrations

```java
// Using factory method (recommended)
Migration migration = Migration.of(2, "Reorganize database settings", ctx -> {
    // Move flat fields into nested structure
    ctx.moveToNested("dbHost", "database", "host");
    ctx.moveToNested("dbPort", "database", "port");
    
    // Add new field with default
    ctx.getOrCreateNestedMap("database").putIfAbsent("poolSize", 10);
    
    // Rename a field
    ctx.rename("oldName", "newName");
});

// Or implement the interface
public class MigrateV2ToV3 implements Migration {
    @Override
    public int targetVersion() { return 3; }
    
    @Override
    public String description() { 
        return "Add caching configuration"; 
    }
    
    @Override
    public void migrate(MigrationContext context) {
        Map<String, Object> cache = context.getOrCreateNestedMap("cache");
        cache.put("enabled", true);
        cache.put("ttlSeconds", 300);
    }
}
```

### MigrationContext Utilities

```java
// Type-safe getters
Optional<String> host = context.getString("host");
Optional<Integer> port = context.getInt("port");
Optional<Boolean> enabled = context.getBoolean("enabled");

// Nested map access
Map<String, Object> database = context.getOrCreateNestedMap("database");
Optional<String> dbHost = context.getNestedValue("database", "host", String.class);

// Field operations
context.rename("oldKey", "newKey");
context.renameNested("section", "oldKey", "newKey");
context.setDefault("newField", "defaultValue");

// Move between levels
context.moveToNested("flatField", "section", "nestedField");
context.moveFromNested("section", "nestedField", "flatField");

// Deep copy for backup
Map<String, Object> backup = context.copyData();
```

### Migration Result Handling

```java
MigrationResult result = migrator.migrate(configPath, targetVersion);

if (result.isSuccess()) {
    System.out.println("Migrated from v" + result.fromVersion() + 
                       " to v" + result.toVersion());
    
    for (MigrationResult.MigrationStep step : result.steps()) {
        System.out.println("  - " + step.description() + 
                           " (" + step.duration().toMillis() + "ms)");
    }
} else {
    MigrationException error = result.error().orElseThrow();
    System.err.println("Migration failed: " + error.getMessage());
    // Restore from backup if needed
}
```

### Raw YAML Loading

For direct YAML manipulation without ConfigLib:

```java
// Load raw YAML
Map<String, Object> data = RawYamlLoader.load(configPath);

// Read version
int version = RawYamlLoader.extractVersion(data);

// Modify data
data.put("newField", "value");

// Save back
RawYamlLoader.save(configPath, data);

// Convert to/from strings
String yaml = RawYamlLoader.saveToString(data);
Map<String, Object> parsed = RawYamlLoader.loadFromString(yaml);
```

## API Documentation

Full Javadoc available at: **https://6b6t.github.io/6b6t-commons/**

## Related Modules

- [commons-database](../commons-database) - Includes `DatabaseConfig` for use with ConfigLoader
