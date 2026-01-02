package net.blockhost.commons.config.migration;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

/// Registry for managing configuration migrations indexed by version.
///
/// The registry stores migrations keyed by their target version, allowing efficient
/// lookup and sequential application of migrations from any source version to a
/// target version.
///
/// ## Registration
///
/// Migrations are registered with their target version as the key:
///
/// ```java
/// MigrationRegistry registry = MigrationRegistry.create();
///
/// // Register individual migrations
/// registry.register(new MigrateV1ToV2());
/// registry.register(new MigrateV2ToV3());
///
/// // Or use the fluent API
/// registry
///     .register(Migration.of(2, "Add timeout field", ctx -> {
///         ctx.data().putIfAbsent("timeout", 30);
///     }))
///     .register(Migration.of(3, "Rename host to hostname", ctx -> {
///         ctx.rename("host", "hostname");
///     }));
/// ```
///
/// ## Version Lookup
///
/// The registry provides methods to find migrations for a given version range:
///
/// ```java
/// // Get all migrations needed to go from version 1 to version 5
/// List<Migration> migrations = registry.getMigrationsInRange(1, 5);
///
/// // Check if a migration exists for a specific target version
/// Optional<Migration> migration = registry.get(3);
/// ```
///
/// ## Thread Safety
///
/// The registry is thread-safe for concurrent registration and lookup operations.
/// Use [#create()] for a thread-safe implementation, or [#createNonConcurrent()]
/// for a non-thread-safe but slightly faster implementation.
///
/// @see Migration
/// @see MigrationExecutor
/// @see ConfigMigrator
public final class MigrationRegistry {

    private final NavigableMap<Integer, Migration> migrations;

    private MigrationRegistry(NavigableMap<Integer, Migration> migrations) {
        this.migrations = migrations;
    }

    /// Creates a new thread-safe migration registry.
    ///
    /// @return a new empty registry
    public static MigrationRegistry create() {
        return new MigrationRegistry(new ConcurrentSkipListMap<>());
    }

    /// Creates a new non-thread-safe migration registry.
    ///
    /// Use this when migrations are only registered during initialization
    /// and the registry won't be modified after that.
    ///
    /// @return a new empty registry
    public static MigrationRegistry createNonConcurrent() {
        return new MigrationRegistry(new TreeMap<>());
    }

    /// Creates a registry pre-populated with the given migrations.
    ///
    /// @param migrations the migrations to register
    /// @return a new registry containing the migrations
    /// @throws NullPointerException if migrations is null
    /// @throws IllegalArgumentException if multiple migrations have the same target version
    public static MigrationRegistry of(Migration... migrations) {
        Objects.requireNonNull(migrations, "migrations");
        MigrationRegistry registry = create();
        for (Migration migration : migrations) {
            registry.register(migration);
        }
        return registry;
    }

    /// Creates a registry pre-populated with the given migrations.
    ///
    /// @param migrations the migrations to register
    /// @return a new registry containing the migrations
    /// @throws NullPointerException if migrations is null
    /// @throws IllegalArgumentException if multiple migrations have the same target version
    public static MigrationRegistry of(Collection<? extends Migration> migrations) {
        Objects.requireNonNull(migrations, "migrations");
        MigrationRegistry registry = create();
        for (Migration migration : migrations) {
            registry.register(migration);
        }
        return registry;
    }

    /// Registers a migration.
    ///
    /// The migration is indexed by its [Migration#targetVersion()].
    ///
    /// @param migration the migration to register
    /// @return this registry for method chaining
    /// @throws NullPointerException if migration is null
    /// @throws IllegalArgumentException if a migration is already registered for the target version
    public MigrationRegistry register(Migration migration) {
        Objects.requireNonNull(migration, "migration");
        int targetVersion = migration.targetVersion();

        Migration existing = migrations.putIfAbsent(targetVersion, migration);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Migration already registered for target version %d".formatted(targetVersion));
        }

        return this;
    }

    /// Registers a migration, replacing any existing migration for the same target version.
    ///
    /// @param migration the migration to register
    /// @return the previously registered migration, or null if none
    /// @throws NullPointerException if migration is null
    public @Nullable Migration registerOrReplace(Migration migration) {
        Objects.requireNonNull(migration, "migration");
        return migrations.put(migration.targetVersion(), migration);
    }

    /// Registers multiple migrations.
    ///
    /// @param migrations the migrations to register
    /// @return this registry for method chaining
    /// @throws NullPointerException if migrations is null
    /// @throws IllegalArgumentException if a migration is already registered for any target version
    public MigrationRegistry registerAll(Migration... migrations) {
        Objects.requireNonNull(migrations, "migrations");
        for (Migration migration : migrations) {
            register(migration);
        }
        return this;
    }

    /// Registers multiple migrations.
    ///
    /// @param migrations the migrations to register
    /// @return this registry for method chaining
    /// @throws NullPointerException if migrations is null
    /// @throws IllegalArgumentException if a migration is already registered for any target version
    public MigrationRegistry registerAll(Collection<? extends Migration> migrations) {
        Objects.requireNonNull(migrations, "migrations");
        for (Migration migration : migrations) {
            register(migration);
        }
        return this;
    }

    /// Gets the migration for a specific target version.
    ///
    /// @param targetVersion the target version
    /// @return an Optional containing the migration, or empty if not found
    public Optional<Migration> get(int targetVersion) {
        return Optional.ofNullable(migrations.get(targetVersion));
    }

    /// Checks if a migration exists for the specified target version.
    ///
    /// @param targetVersion the target version to check
    /// @return true if a migration is registered for that version
    public boolean contains(int targetVersion) {
        return migrations.containsKey(targetVersion);
    }

    /// Removes the migration for the specified target version.
    ///
    /// @param targetVersion the target version
    /// @return the removed migration, or null if none was registered
    public @Nullable Migration remove(int targetVersion) {
        return migrations.remove(targetVersion);
    }

    /// Clears all registered migrations.
    public void clear() {
        migrations.clear();
    }

    /// Returns the number of registered migrations.
    ///
    /// @return the migration count
    public int size() {
        return migrations.size();
    }

    /// Checks if the registry is empty.
    ///
    /// @return true if no migrations are registered
    public boolean isEmpty() {
        return migrations.isEmpty();
    }

    /// Returns all registered migrations in version order.
    ///
    /// @return an unmodifiable list of migrations sorted by target version
    public List<Migration> getAllMigrations() {
        return List.copyOf(migrations.values());
    }

    /// Returns all registered target versions in ascending order.
    ///
    /// @return an unmodifiable set of target versions
    public List<Integer> getAllVersions() {
        return List.copyOf(migrations.keySet());
    }

    /// Gets the highest registered target version.
    ///
    /// @return an Optional containing the highest version, or empty if registry is empty
    public Optional<Integer> getHighestVersion() {
        return migrations.isEmpty() ? Optional.empty() : Optional.of(migrations.lastKey());
    }

    /// Gets the lowest registered target version.
    ///
    /// @return an Optional containing the lowest version, or empty if registry is empty
    public Optional<Integer> getLowestVersion() {
        return migrations.isEmpty() ? Optional.empty() : Optional.of(migrations.firstKey());
    }

    /// Gets all migrations needed to migrate from one version to another.
    ///
    /// Returns migrations where `sourceVersion < targetVersion <= toVersion`,
    /// ensuring migrations are applicable for configs at the given source version.
    ///
    /// @param fromVersion the current config version (exclusive - migrations after this)
    /// @param toVersion the target version (inclusive)
    /// @return a list of migrations in version order
    /// @throws MigrationException if fromVersion >= toVersion
    public List<Migration> getMigrationsInRange(int fromVersion, int toVersion) {
        if (fromVersion >= toVersion) {
            if (fromVersion == toVersion) {
                return List.of();
            }
            throw MigrationException.invalidVersionRange(fromVersion, toVersion);
        }

        // Get migrations where targetVersion is in range (fromVersion, toVersion]
        NavigableMap<Integer, Migration> subMap = migrations.subMap(fromVersion, false, toVersion, true);
        return List.copyOf(subMap.values());
    }

    /// Validates that all migrations needed to go from one version to another exist.
    ///
    /// This checks that there are no gaps in the migration chain.
    ///
    /// @param fromVersion the starting version
    /// @param toVersion the target version
    /// @return a list of missing version numbers, empty if chain is complete
    public List<Integer> findMissingMigrations(int fromVersion, int toVersion) {
        if (fromVersion >= toVersion) {
            return List.of();
        }

        List<Integer> missing = new ArrayList<>();
        for (int version = fromVersion + 1; version <= toVersion; version++) {
            if (!migrations.containsKey(version)) {
                missing.add(version);
            }
        }
        return missing;
    }

    /// Checks if the registry has a complete migration chain between two versions.
    ///
    /// @param fromVersion the starting version
    /// @param toVersion the target version
    /// @return true if migrations exist for all versions in the range
    public boolean hasCompleteMigrationChain(int fromVersion, int toVersion) {
        return findMissingMigrations(fromVersion, toVersion).isEmpty();
    }

    /// Creates an immutable copy of this registry.
    ///
    /// @return a new immutable registry with the same migrations
    public MigrationRegistry immutableCopy() {
        return new MigrationRegistry(Collections.unmodifiableNavigableMap(new TreeMap<>(migrations)));
    }

    @Override
    public String toString() {
        if (migrations.isEmpty()) {
            return "MigrationRegistry[empty]";
        }
        return "MigrationRegistry[versions=%s]".formatted(migrations.keySet());
    }
}
