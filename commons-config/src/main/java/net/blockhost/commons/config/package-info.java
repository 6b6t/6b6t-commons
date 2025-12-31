/// Configuration utilities for 6b6t plugins.
///
/// This package provides utilities for loading, managing, and migrating YAML configuration
/// files using ConfigLib and SnakeYAML. It simplifies common configuration patterns and
/// provides a consistent API across plugins.
///
/// ## Features
///
/// - **Simple loading**: Load and save configs with [ConfigLoader]
/// - **Version tracking**: Extend [VersionAwareConfiguration] for versioned configs
/// - **Migration support**: Full migration framework in the [migration][net.blockhost.commons.config.migration] subpackage
///
/// ## Quick Start
///
/// ### Basic Configuration
///
/// Define your configuration class using ConfigLib annotations:
///
/// ```java
/// @Configuration
/// public class MyPluginConfig {
///     @Comment("The server host address")
///     private String host = "localhost";
///
///     @Comment("The server port")
///     private int port = 3306;
///
///     @Comment("Database settings")
///     private DatabaseSettings database = new DatabaseSettings();
/// }
/// ```
///
/// Load your configuration:
///
/// ```java
/// Path configPath = plugin.getDataFolder().toPath().resolve("config.yml");
/// MyPluginConfig config = ConfigLoader.loadOrCreate(configPath, MyPluginConfig.class);
/// ```
///
/// ### Versioned Configuration with Migration
///
/// For configurations that may change over time, use [VersionAwareConfiguration]:
///
/// ```java
/// @Configuration
/// public class MyPluginConfig extends VersionAwareConfiguration {
///     private static final int CURRENT_VERSION = 2;
///
///     private String hostname = "localhost";
///     private int timeout = 30;
///
///     public MyPluginConfig() {
///         super(CURRENT_VERSION);
///     }
/// }
/// ```
///
/// Set up migrations and load:
///
/// ```java
/// ConfigMigrator migrator = ConfigMigrator.builder()
///     .createBackups(true)
///     .register(Migration.of(2, "Rename host to hostname", ctx -> {
///         ctx.rename("host", "hostname");
///     }))
///     .build();
///
/// MyPluginConfig config = migrator.migrateAndLoad(configPath, MyPluginConfig.class, 2);
/// ```
///
/// ## Package Structure
///
/// - [ConfigLoader] - Core utility for loading/saving ConfigLib configurations
/// - [VersionAwareConfiguration] - Base class for versioned configurations
/// - [net.blockhost.commons.config.migration] - Complete migration framework
///
/// @see ConfigLoader
/// @see VersionAwareConfiguration
/// @see net.blockhost.commons.config.migration.ConfigMigrator
/// @see de.exlll.configlib.Configuration
@NullMarked
package net.blockhost.commons.config;

import org.jspecify.annotations.NullMarked;
