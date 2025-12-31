package net.blockhost.commons.config.migration;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import net.blockhost.commons.config.ConfigLoader;
import net.blockhost.commons.config.VersionAwareConfiguration;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/// High-level API for migrating configuration files.
///
/// `ConfigMigrator` combines [RawYamlLoader], [MigrationRegistry], and [MigrationExecutor]
/// into a simple, easy-to-use API for migrating [VersionAwareConfiguration] files.
///
/// ## Basic Usage
///
/// ```java
/// // Create a migrator with your migrations
/// ConfigMigrator migrator = ConfigMigrator.builder()
///     .register(Migration.of(2, "Add timeout field", ctx -> {
///         ctx.data().putIfAbsent("timeout", 30);
///     }))
///     .register(Migration.of(3, "Rename host to hostname", ctx -> {
///         ctx.rename("host", "hostname");
///     }))
///     .build();
///
/// // Migrate and load config
/// MyConfig config = migrator.migrateAndLoad(configPath, MyConfig.class);
/// ```
///
/// ## Backup Support
///
/// The migrator can create backups before applying migrations:
///
/// ```java
/// ConfigMigrator migrator = ConfigMigrator.builder()
///     .createBackups(true)              // Enable backups
///     .backupSuffix(".backup")          // Customize suffix (default: .bak)
///     .register(...)
///     .build();
/// ```
///
/// ## Migration Callbacks
///
/// Monitor migration progress with callbacks:
///
/// ```java
/// ConfigMigrator migrator = ConfigMigrator.builder()
///     .beforeMigration(m -> logger.info("Applying: " + m.description()))
///     .afterMigration((m, ctx) -> logger.info("Applied migration to v" + m.targetVersion()))
///     .onError(e -> logger.error("Migration failed", e))
///     .register(...)
///     .build();
/// ```
///
/// ## Complete Example
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
///
/// // Setup migrator
/// ConfigMigrator migrator = ConfigMigrator.builder()
///     .createBackups(true)
///     .register(Migration.of(2, "Add timeout", ctx -> {
///         ctx.setDefault("timeout", 30);
///     }))
///     .register(Migration.of(3, "Rename host to hostname", ctx -> {
///         ctx.rename("host", "hostname");
///     }))
///     .build();
///
/// // Load with automatic migration
/// Path configPath = dataFolder.resolve("config.yml");
/// ServerConfig config = migrator.migrateAndLoad(configPath, ServerConfig.class, 3);
/// ```
///
/// @see Migration
/// @see VersionAwareConfiguration
/// @see MigrationResult
public final class ConfigMigrator {

    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final MigrationRegistry registry;
    private final MigrationExecutor executor;
    private final boolean createBackups;
    private final String backupSuffix;
    private final boolean useTimestampedBackups;

    private ConfigMigrator(Builder builder) {
        this.registry = builder.registry;
        this.executor = MigrationExecutor.create(registry)
                .strictMode(builder.strictMode)
                .beforeMigration(builder.beforeMigrationCallback)
                .afterMigration(builder.afterMigrationCallback)
                .onError(builder.errorCallback);
        this.createBackups = builder.createBackups;
        this.backupSuffix = builder.backupSuffix;
        this.useTimestampedBackups = builder.useTimestampedBackups;
    }

    /// Creates a new migrator builder.
    ///
    /// @return a new builder
    public static Builder builder() {
        return new Builder();
    }

    /// Creates a simple migrator with the given migrations.
    ///
    /// @param migrations the migrations to register
    /// @return a new migrator
    public static ConfigMigrator of(Migration... migrations) {
        return builder().registerAll(migrations).build();
    }

    /// Migrates a configuration file to the target version and loads it.
    ///
    /// This is the primary method for loading versioned configurations. It:
    /// 1. Loads the raw YAML data
    /// 2. Reads the current version
    /// 3. Applies necessary migrations
    /// 4. Saves the migrated data
    /// 5. Loads the configuration using ConfigLib
    ///
    /// @param path the path to the configuration file
    /// @param configClass the configuration class (must extend [VersionAwareConfiguration])
    /// @param targetVersion the target version to migrate to
    /// @param <T> the configuration type
    /// @return the loaded and migrated configuration
    /// @throws MigrationException if migration or loading fails
    /// @throws NullPointerException if any parameter is null
    public <T extends VersionAwareConfiguration> T migrateAndLoad(
            Path path, Class<T> configClass, int targetVersion) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");

        // If file doesn't exist, just create with defaults
        if (!Files.exists(path)) {
            return ConfigLoader.loadOrCreate(path, configClass);
        }

        // Load raw data and migrate
        MigrationResult result = migrate(path, targetVersion);

        if (!result.isSuccess()) {
            throw result.error().orElseGet(() -> new MigrationException("Migration failed with unknown error"));
        }

        // Load the migrated configuration
        return ConfigLoader.load(path, configClass);
    }

    /// Migrates a configuration file to the target version and loads it with custom properties.
    ///
    /// @param path the path to the configuration file
    /// @param configClass the configuration class
    /// @param targetVersion the target version to migrate to
    /// @param properties custom ConfigLib properties
    /// @param <T> the configuration type
    /// @return the loaded and migrated configuration
    /// @throws MigrationException if migration or loading fails
    public <T extends VersionAwareConfiguration> T migrateAndLoad(
            Path path, Class<T> configClass, int targetVersion, YamlConfigurationProperties properties) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(properties, "properties");

        if (!Files.exists(path)) {
            return YamlConfigurations.update(path, configClass, properties);
        }

        MigrationResult result = migrate(path, targetVersion);

        if (!result.isSuccess()) {
            throw result.error().orElseGet(() -> new MigrationException("Migration failed with unknown error"));
        }

        return YamlConfigurations.load(path, configClass, properties);
    }

    /// Migrates a configuration file to the target version.
    ///
    /// This method only performs migration without loading the result into a typed class.
    /// Use this when you need more control over the process or want to inspect the
    /// migration result before loading.
    ///
    /// @param path the path to the configuration file
    /// @param targetVersion the target version to migrate to
    /// @return the migration result
    /// @throws NullPointerException if path is null
    public MigrationResult migrate(Path path, int targetVersion) {
        Objects.requireNonNull(path, "path");

        // Load raw data
        Map<String, Object> data = RawYamlLoader.load(path);

        // Handle empty/new file
        if (data.isEmpty()) {
            data.put("version", targetVersion);
            RawYamlLoader.save(path, data);
            return MigrationResult.noMigrationNeeded(targetVersion, data);
        }

        // Get current version
        int currentVersion = RawYamlLoader.extractVersion(data);

        // No migration needed
        if (currentVersion == targetVersion) {
            return MigrationResult.noMigrationNeeded(currentVersion, data);
        }

        // Create backup if enabled
        if (createBackups && currentVersion < targetVersion) {
            createBackup(path);
        }

        // Execute migrations
        MigrationResult result = executor.execute(path, data, currentVersion, targetVersion);

        // Save migrated data on success
        if (result.isSuccess()) {
            RawYamlLoader.save(path, result.data());
        }

        return result;
    }

    /// Migrates in-memory data without file I/O.
    ///
    /// @param data the raw YAML data to migrate
    /// @param fromVersion the current version
    /// @param toVersion the target version
    /// @return the migration result
    /// @throws NullPointerException if data is null
    public MigrationResult migrateData(Map<String, Object> data, int fromVersion, int toVersion) {
        Objects.requireNonNull(data, "data");
        return executor.execute(data, fromVersion, toVersion);
    }

    /// Checks if migrations are needed for a configuration file.
    ///
    /// @param path the path to the configuration file
    /// @param targetVersion the target version
    /// @return true if the file needs migration
    /// @throws NullPointerException if path is null
    public boolean needsMigration(Path path, int targetVersion) {
        Objects.requireNonNull(path, "path");

        if (!Files.exists(path)) {
            return false;
        }

        int currentVersion = RawYamlLoader.readVersion(path);
        return currentVersion < targetVersion;
    }

    /// Gets the current version of a configuration file.
    ///
    /// @param path the path to the configuration file
    /// @return an Optional containing the version, or empty if file doesn't exist
    /// @throws NullPointerException if path is null
    public Optional<Integer> getVersion(Path path) {
        Objects.requireNonNull(path, "path");

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        return Optional.of(RawYamlLoader.readVersion(path));
    }

    /// Checks if all migrations are available for the given version range.
    ///
    /// @param fromVersion the starting version
    /// @param toVersion the target version
    /// @return true if all migrations are available
    public boolean canMigrate(int fromVersion, int toVersion) {
        return executor.canMigrate(fromVersion, toVersion);
    }

    /// Returns the migration registry.
    ///
    /// @return the registry
    public MigrationRegistry registry() {
        return registry;
    }

    private void createBackup(Path path) {
        try {
            String backupName;
            if (useTimestampedBackups) {
                String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
                String fileName = path.getFileName().toString();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    backupName = fileName.substring(0, dotIndex) + "-" + timestamp + fileName.substring(dotIndex) + backupSuffix;
                } else {
                    backupName = fileName + "-" + timestamp + backupSuffix;
                }
            } else {
                backupName = path.getFileName().toString() + backupSuffix;
            }

            Path backupPath = path.resolveSibling(backupName);
            Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new MigrationException("Failed to create backup of " + path, e);
        }
    }

    /// Builder for creating [ConfigMigrator] instances.
    public static final class Builder {

        private final MigrationRegistry registry = MigrationRegistry.create();
        private boolean strictMode = true;
        private boolean createBackups = false;
        private String backupSuffix = ".bak";
        private boolean useTimestampedBackups = false;
        private @Nullable Consumer<Migration> beforeMigrationCallback;
        private @Nullable MigrationExecutor.MigrationCallback afterMigrationCallback;
        private @Nullable Consumer<MigrationException> errorCallback;

        private Builder() {}

        /// Registers a migration.
        ///
        /// @param migration the migration to register
        /// @return this builder
        public Builder register(Migration migration) {
            registry.register(migration);
            return this;
        }

        /// Registers multiple migrations.
        ///
        /// @param migrations the migrations to register
        /// @return this builder
        public Builder registerAll(Migration... migrations) {
            registry.registerAll(migrations);
            return this;
        }

        /// Enables or disables strict mode.
        ///
        /// In strict mode (default), migration fails if any step is missing.
        ///
        /// @param strict true for strict mode
        /// @return this builder
        public Builder strictMode(boolean strict) {
            this.strictMode = strict;
            return this;
        }

        /// Enables or disables backup creation.
        ///
        /// @param createBackups true to create backups before migration
        /// @return this builder
        public Builder createBackups(boolean createBackups) {
            this.createBackups = createBackups;
            return this;
        }

        /// Sets the backup file suffix.
        ///
        /// @param suffix the suffix to append to backup files (default: ".bak")
        /// @return this builder
        public Builder backupSuffix(String suffix) {
            this.backupSuffix = Objects.requireNonNull(suffix, "suffix");
            return this;
        }

        /// Enables timestamped backups.
        ///
        /// When enabled, backup files include a timestamp in the filename,
        /// preventing overwrites of previous backups.
        ///
        /// @param timestamped true for timestamped backups
        /// @return this builder
        public Builder useTimestampedBackups(boolean timestamped) {
            this.useTimestampedBackups = timestamped;
            return this;
        }

        /// Sets a callback to be invoked before each migration.
        ///
        /// @param callback the callback
        /// @return this builder
        public Builder beforeMigration(Consumer<Migration> callback) {
            this.beforeMigrationCallback = callback;
            return this;
        }

        /// Sets a callback to be invoked after each successful migration.
        ///
        /// @param callback the callback
        /// @return this builder
        public Builder afterMigration(MigrationExecutor.MigrationCallback callback) {
            this.afterMigrationCallback = callback;
            return this;
        }

        /// Sets a callback to be invoked when a migration fails.
        ///
        /// @param callback the callback
        /// @return this builder
        public Builder onError(Consumer<MigrationException> callback) {
            this.errorCallback = callback;
            return this;
        }

        /// Builds the ConfigMigrator.
        ///
        /// @return the configured migrator
        public ConfigMigrator build() {
            return new ConfigMigrator(this);
        }
    }
}
