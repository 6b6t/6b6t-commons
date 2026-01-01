package net.blockhost.commons.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.blockhost.commons.config.migration.ConfigMigrator;
import net.blockhost.commons.config.migration.Migration;
import net.blockhost.commons.config.migration.MigrationRegistry;

/// Base class for configuration files that support versioned migrations.
///
/// Extend this class to enable automatic migration support for your configuration.
/// The version field tracks the current schema version of the configuration file,
/// allowing the migration system to determine which migrations need to be applied.
///
/// ## Usage
///
/// ```java
/// @Configuration
/// public class MyPluginConfig extends VersionAwareConfiguration {
///
///     // Current schema version - increment when making breaking changes
///     private static final int CURRENT_VERSION = 3;
///
///     private String serverName = "My Server";
///     private int maxPlayers = 100;
///
///     public MyPluginConfig() {
///         super(CURRENT_VERSION);
///     }
/// }
/// ```
///
/// ## Version Field Behavior
///
/// - When a new config is created, it starts at the `defaultVersion` passed to the constructor
/// - When an existing config is loaded, the version reflects the file's current schema version
/// - After migrations are applied, the version is automatically updated to the target version
/// - Users should **never** manually modify the version field in the YAML file
///
/// ## Migration Flow
///
/// 1. Config file is loaded with [RawYamlLoader]
/// 2. Version is read from the raw YAML data
/// 3. [ConfigMigrator] applies all migrations from current version to target version
/// 4. Updated YAML is saved back to disk
/// 5. Config is then loaded normally via [ConfigLoader]
///
/// @see Migration
/// @see ConfigMigrator
/// @see MigrationRegistry
@Configuration
@Getter
@Accessors(fluent = true)
public abstract class VersionAwareConfiguration {

    @Comment({
        "Configuration schema version - DO NOT MODIFY THIS VALUE",
        "This field is managed automatically by the migration system.",
        "Changing it manually may cause data loss or corruption."
    })
    private int version;

    /// Creates a new version-aware configuration with the specified default version.
    ///
    /// The `defaultVersion` parameter should be set to the **current/newest** schema version
    /// of your configuration. This ensures that newly created configuration files start
    /// with the latest schema version and don't require any migrations.
    ///
    /// @param defaultVersion the current schema version for new configurations
    protected VersionAwareConfiguration(int defaultVersion) {
        this.version = defaultVersion;
    }

    /// Returns the current schema version of this configuration.
    ///
    /// This value represents either:
    /// - The default version (for newly created configs)
    /// - The version read from an existing config file
    /// - The updated version after migrations have been applied
    ///
    /// @return the current configuration schema version
    public int version() {
        return version;
    }
}
