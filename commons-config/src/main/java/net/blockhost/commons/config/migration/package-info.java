/// Configuration migration framework for versioned YAML configurations.
///
/// This package provides a complete solution for managing configuration schema changes
/// over time. It allows you to define migrations that transform configuration data
/// from older versions to newer versions, ensuring backward compatibility as your
/// application evolves.
///
/// ## Overview
///
/// The migration system is built around these core components:
///
/// | Component | Purpose |
/// |-----------|---------|
/// | [Migration] | Defines a single migration step between versions |
/// | [MigrationContext] | Provides access to config data and utility methods |
/// | [MigrationRegistry] | Stores and manages registered migrations |
/// | [MigrationExecutor] | Executes migrations in sequence |
/// | [ConfigMigrator] | High-level API combining all components |
/// | [RawYamlLoader] | Low-level YAML loading/saving with SnakeYAML |
///
/// ## Quick Start
///
/// ### 1. Define a versioned configuration
///
/// Extend [net.blockhost.commons.config.VersionAwareConfiguration] to add version tracking:
///
/// ```java
/// @Configuration
/// public class ServerConfig extends VersionAwareConfiguration {
///     private static final int CURRENT_VERSION = 3;
///
///     private String hostname = "localhost";
///     private int port = 25565;
///     private int timeout = 30;
///
///     public ServerConfig() {
///         super(CURRENT_VERSION);
///     }
/// }
/// ```
///
/// ### 2. Create migrations
///
/// Implement [Migration] or use the factory methods:
///
/// ```java
/// Migration addTimeout = Migration.of(2, "Add timeout field", ctx -> {
///     ctx.setDefault("timeout", 30);
/// });
///
/// Migration renameHost = Migration.of(3, "Rename host to hostname", ctx -> {
///     ctx.rename("host", "hostname");
/// });
/// ```
///
/// ### 3. Configure and use the migrator
///
/// ```java
/// ConfigMigrator migrator = ConfigMigrator.builder()
///     .createBackups(true)
///     .register(addTimeout)
///     .register(renameHost)
///     .beforeMigration(m -> logger.info("Applying: " + m.description()))
///     .build();
///
/// // Load config with automatic migration
/// ServerConfig config = migrator.migrateAndLoad(
///     configPath,
///     ServerConfig.class,
///     ServerConfig.CURRENT_VERSION
/// );
/// ```
///
/// ## Migration Implementation
///
/// Migrations transform raw YAML data represented as `Map<String, Object>`:
///
/// ```java
/// public class MigrateV2ToV3 implements Migration {
///
///     @Override
///     public int targetVersion() {
///         return 3;
///     }
///
///     @Override
///     public String description() {
///         return "Reorganize database settings into nested structure";
///     }
///
///     @Override
///     public void migrate(MigrationContext context) {
///         // Move flat fields into a nested 'database' section
///         context.moveToNested("dbHost", "database", "host");
///         context.moveToNested("dbPort", "database", "port");
///         context.moveToNested("dbName", "database", "name");
///
///         // Add new field with default
///         context.getOrCreateNestedMap("database")
///             .putIfAbsent("poolSize", 10);
///     }
/// }
/// ```
///
/// ## Version Semantics
///
/// - Version `0` indicates a config without version tracking (legacy)
/// - Version `1` is typically the first tracked version
/// - Migrations are applied sequentially: 1 -> 2 -> 3 -> ...
/// - The [Migration#sourceVersion()] defaults to `targetVersion - 1`
/// - After migration, the `version` field is automatically updated
///
/// ## Error Handling
///
/// Migrations can fail for various reasons. The system provides:
///
/// - [MigrationException] for all migration-related errors
/// - [MigrationResult] to inspect success/failure details
/// - Optional backup creation before migration
/// - Detailed step-by-step progress tracking
///
/// ```java
/// MigrationResult result = migrator.migrate(configPath, 5);
/// if (!result.isSuccess()) {
///     MigrationException error = result.error().orElseThrow();
///     logger.error("Migration failed at version " + result.toVersion(), error);
///
///     // Restore from backup if needed
/// }
/// ```
///
/// ## Thread Safety
///
/// - [MigrationRegistry] is thread-safe for concurrent access
/// - [MigrationExecutor] and [ConfigMigrator] are not thread-safe
/// - Create separate executor/migrator instances for concurrent use
///
/// @see net.blockhost.commons.config.VersionAwareConfiguration
/// @see net.blockhost.commons.config.ConfigLoader
@NullMarked
package net.blockhost.commons.config.migration;

import org.jspecify.annotations.NullMarked;
