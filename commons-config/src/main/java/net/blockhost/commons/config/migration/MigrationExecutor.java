package net.blockhost.commons.config.migration;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/// Executes configuration migrations sequentially by version.
///
/// The executor is responsible for applying migrations from a registry in the correct
/// order, tracking progress, handling errors, and optionally creating backups or
/// performing rollbacks.
///
/// ## Basic Usage
///
/// ```java
/// MigrationRegistry registry = MigrationRegistry.create()
///     .register(new MigrateV1ToV2())
///     .register(new MigrateV2ToV3());
///
/// MigrationExecutor executor = MigrationExecutor.create(registry);
///
/// Map<String, Object> data = RawYamlLoader.load(configPath);
/// int currentVersion = RawYamlLoader.extractVersion(data);
///
/// MigrationResult result = executor.execute(configPath, data, currentVersion, 3);
/// if (result.isSuccess()) {
///     RawYamlLoader.save(configPath, result.data());
/// }
/// ```
///
/// ## Execution Modes
///
/// The executor supports different execution modes:
///
/// - **Strict mode** (default): Fails if any migration in the chain is missing
/// - **Lenient mode**: Skips missing migrations and applies available ones
/// - **Dry run**: Simulates migrations without modifying the data
///
/// ## Callbacks
///
/// Register callbacks to monitor migration progress:
///
/// ```java
/// executor.beforeMigration(migration -> {
///     logger.info("Applying migration: " + migration.description());
/// });
///
/// executor.afterMigration((migration, duration) -> {
///     logger.info("Completed in " + duration.toMillis() + "ms");
/// });
/// ```
///
/// @see Migration
/// @see MigrationRegistry
/// @see MigrationResult
public final class MigrationExecutor {

    private final MigrationRegistry registry;
    private boolean strictMode = true;
    private boolean dryRun = false;
    private @Nullable Consumer<Migration> beforeMigrationCallback;
    private @Nullable MigrationCallback afterMigrationCallback;
    private @Nullable Consumer<MigrationException> errorCallback;

    private MigrationExecutor(MigrationRegistry registry) {
        this.registry = registry;
    }

    /// Creates a new migration executor with the specified registry.
    ///
    /// @param registry the migration registry to use
    /// @return a new executor
    /// @throws NullPointerException if registry is null
    public static MigrationExecutor create(MigrationRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        return new MigrationExecutor(registry);
    }

    /// Enables or disables strict mode.
    ///
    /// In strict mode (default), the executor fails if any migration in the chain
    /// is missing. In lenient mode, missing migrations are skipped.
    ///
    /// @param strict true for strict mode, false for lenient mode
    /// @return this executor for method chaining
    public MigrationExecutor strictMode(boolean strict) {
        this.strictMode = strict;
        return this;
    }

    /// Enables or disables dry run mode.
    ///
    /// In dry run mode, migrations are executed but results are discarded.
    /// The original data is preserved. Useful for testing migrations.
    ///
    /// @param dryRun true to enable dry run mode
    /// @return this executor for method chaining
    public MigrationExecutor dryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    /// Sets a callback to be invoked before each migration.
    ///
    /// @param callback the callback to invoke
    /// @return this executor for method chaining
    public MigrationExecutor beforeMigration(@Nullable Consumer<Migration> callback) {
        this.beforeMigrationCallback = callback;
        return this;
    }

    /// Sets a callback to be invoked after each successful migration.
    ///
    /// @param callback the callback to invoke
    /// @return this executor for method chaining
    public MigrationExecutor afterMigration(@Nullable MigrationCallback callback) {
        this.afterMigrationCallback = callback;
        return this;
    }

    /// Sets a callback to be invoked when a migration fails.
    ///
    /// @param callback the callback to invoke
    /// @return this executor for method chaining
    public MigrationExecutor onError(@Nullable Consumer<MigrationException> callback) {
        this.errorCallback = callback;
        return this;
    }

    /// Executes migrations on the provided data.
    ///
    /// @param filePath the path to the config file (for context/logging)
    /// @param data the raw YAML data to migrate
    /// @param fromVersion the current version of the data
    /// @param toVersion the target version to migrate to
    /// @return the migration result
    /// @throws NullPointerException if filePath or data is null
    public MigrationResult execute(Path filePath, Map<String, Object> data, int fromVersion, int toVersion) {
        Objects.requireNonNull(filePath, "filePath");
        Objects.requireNonNull(data, "data");

        // No migration needed
        if (fromVersion == toVersion) {
            return MigrationResult.noMigrationNeeded(fromVersion, data);
        }

        // Validate version range
        if (fromVersion > toVersion) {
            return MigrationResult.failure(
                    fromVersion,
                    fromVersion,
                    List.of(),
                    java.time.Duration.ZERO,
                    data,
                    MigrationException.invalidVersionRange(fromVersion, toVersion));
        }

        // Get migrations in range
        List<Migration> migrations = registry.getMigrationsInRange(fromVersion, toVersion);

        // Check for missing migrations in strict mode
        if (strictMode) {
            List<Integer> missing = registry.findMissingMigrations(fromVersion, toVersion);
            if (!missing.isEmpty()) {
                return MigrationResult.failure(
                        fromVersion,
                        fromVersion,
                        List.of(),
                        java.time.Duration.ZERO,
                        data,
                        new MigrationException("Missing migrations for versions: " + missing));
            }
        }

        // Create working copy if dry run
        Map<String, Object> workingData = dryRun ? deepCopy(data) : data;

        // Execute migrations
        MigrationResult.Builder resultBuilder = MigrationResult.builder(fromVersion, workingData);

        int currentVersion = fromVersion;
        for (Migration migration : migrations) {
            // Verify migration source version matches current
            if (strictMode && migration.sourceVersion() != currentVersion) {
                MigrationException error = new MigrationException(
                        "Migration version mismatch: expected source version %d, but migration is from %d to %d"
                                .formatted(currentVersion, migration.sourceVersion(), migration.targetVersion()));
                notifyError(error);
                return resultBuilder.fail(currentVersion, error);
            }

            // Create context for this migration
            MigrationContext context = MigrationContext.of(filePath, workingData, currentVersion, toVersion);

            // Invoke before callback
            notifyBeforeMigration(migration);

            // Execute migration
            resultBuilder.startStep();
            try {
                migration.migrate(context);
                currentVersion = migration.targetVersion();
                workingData.put("version", currentVersion);
                resultBuilder.completeStep(migration);
                notifyAfterMigration(migration, context);
            } catch (MigrationException e) {
                notifyError(e);
                return resultBuilder.fail(migration.targetVersion(), e);
            } catch (Exception e) {
                MigrationException wrapped = MigrationException.migrationFailed(migration.targetVersion(), e);
                notifyError(wrapped);
                return resultBuilder.fail(migration.targetVersion(), wrapped);
            }
        }

        // If dry run, return original data
        if (dryRun) {
            return resultBuilder.build(toVersion);
        }

        return resultBuilder.build(currentVersion);
    }

    /// Executes migrations on in-memory data without a file path.
    ///
    /// @param data the raw YAML data to migrate
    /// @param fromVersion the current version of the data
    /// @param toVersion the target version to migrate to
    /// @return the migration result
    /// @throws NullPointerException if data is null
    public MigrationResult execute(Map<String, Object> data, int fromVersion, int toVersion) {
        return execute(Path.of(""), data, fromVersion, toVersion);
    }

    /// Checks if migrations can be executed for the given version range.
    ///
    /// @param fromVersion the current version
    /// @param toVersion the target version
    /// @return true if all required migrations are available
    public boolean canMigrate(int fromVersion, int toVersion) {
        if (fromVersion >= toVersion) {
            return fromVersion == toVersion;
        }
        return registry.hasCompleteMigrationChain(fromVersion, toVersion);
    }

    /// Returns the registry used by this executor.
    ///
    /// @return the migration registry
    public MigrationRegistry registry() {
        return registry;
    }

    private void notifyBeforeMigration(Migration migration) {
        if (beforeMigrationCallback != null) {
            try {
                beforeMigrationCallback.accept(migration);
            } catch (Exception ignored) {
                // Callbacks should not throw, but don't let them break migrations
            }
        }
    }

    private void notifyAfterMigration(Migration migration, MigrationContext context) {
        if (afterMigrationCallback != null) {
            try {
                afterMigrationCallback.onMigrationComplete(migration, context);
            } catch (Exception ignored) {
                // Callbacks should not throw
            }
        }
    }

    private void notifyError(MigrationException error) {
        if (errorCallback != null) {
            try {
                errorCallback.accept(error);
            } catch (Exception ignored) {
                // Callbacks should not throw
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> original) {
        java.util.LinkedHashMap<String, Object> copy = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> map) {
                copy.put(entry.getKey(), deepCopy((Map<String, Object>) map));
            } else if (value instanceof List<?> list) {
                copy.put(entry.getKey(), deepCopyList(list));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    private List<Object> deepCopyList(List<?> original) {
        return original.stream()
                .map(item -> {
                    if (item instanceof Map<?, ?> map) {
                        return deepCopy((Map<String, Object>) map);
                    } else if (item instanceof List<?> list) {
                        return deepCopyList(list);
                    }
                    return item;
                })
                .toList();
    }

    /// Callback interface for post-migration notifications.
    @FunctionalInterface
    public interface MigrationCallback {

        /// Called after a migration completes successfully.
        ///
        /// @param migration the migration that completed
        /// @param context the migration context with updated data
        void onMigrationComplete(Migration migration, MigrationContext context);
    }
}
