package net.blockhost.commons.config.migration;

/// Represents a single configuration migration step from one version to the next.
///
/// Migrations are the core building blocks of the migration system. Each migration
/// is responsible for transforming configuration data from version `N` to version `N+1`.
/// Migrations are applied sequentially in order of their target versions.
///
/// ## Version Semantics
///
/// - [#sourceVersion()] returns the version this migration upgrades **from**
/// - [#targetVersion()] returns the version this migration upgrades **to**
/// - Typically, `targetVersion() == sourceVersion() + 1`, but gaps are allowed
///
/// ## Implementation Guidelines
///
/// 1. **Be idempotent**: Running the same migration twice should produce the same result
/// 2. **Handle missing fields**: Old configs may not have all expected fields
/// 3. **Preserve unknown fields**: Don't remove fields you don't recognize
/// 4. **Document changes**: Use [#description()] to explain what the migration does
///
/// ## Example Implementation
///
/// ```java
/// public class MigrateV1ToV2 implements Migration {
///
///     @Override
///     public int sourceVersion() {
///         return 1;
///     }
///
///     @Override
///     public int targetVersion() {
///         return 2;
///     }
///
///     @Override
///     public String description() {
///         return "Rename 'serverHost' to 'host' and add 'timeout' field";
///     }
///
///     @Override
///     public void migrate(MigrationContext context) {
///         Map<String, Object> data = context.data();
///
///         // Rename field
///         if (data.containsKey("serverHost")) {
///             data.put("host", data.remove("serverHost"));
///         }
///
///         // Add new field with default
///         data.putIfAbsent("timeout", 30);
///     }
/// }
/// ```
///
/// ## Nested Configuration Migration
///
/// For nested configurations, navigate the map structure:
///
/// ```java
/// @Override
/// public void migrate(MigrationContext context) {
///     Map<String, Object> data = context.data();
///
///     // Access nested section
///     @SuppressWarnings("unchecked")
///     Map<String, Object> database = (Map<String, Object>) data.get("database");
///     if (database != null) {
///         database.put("poolSize", database.getOrDefault("maxConnections", 10));
///         database.remove("maxConnections");
///     }
/// }
/// ```
///
/// @see MigrationContext
/// @see MigrationRegistry
/// @see MigrationExecutor
public interface Migration {

    /// Returns the version this migration upgrades from.
    ///
    /// This is the version number that must be present in the configuration
    /// file for this migration to be applicable.
    ///
    /// @return the source version number
    default int sourceVersion() {
        return targetVersion() - 1;
    }

    /// Returns the version this migration upgrades to.
    ///
    /// After this migration is successfully applied, the configuration's
    /// version field will be updated to this value.
    ///
    /// @return the target version number
    int targetVersion();

    /// Returns a human-readable description of what this migration does.
    ///
    /// This description is used for logging and debugging purposes.
    /// It should briefly explain the changes being made.
    ///
    /// @return a description of the migration, or a default message if not overridden
    default String description() {
        return "Migrate from version %d to version %d".formatted(sourceVersion(), targetVersion());
    }

    /// Performs the migration on the provided context.
    ///
    /// This method should modify the data in the [MigrationContext] to transform
    /// it from [#sourceVersion()] format to [#targetVersion()] format. The version
    /// field itself is updated automatically by the migration executor.
    ///
    /// If the migration cannot be completed, throw a [MigrationException] with
    /// a descriptive message. This will abort the migration process and may
    /// trigger a rollback if enabled.
    ///
    /// @param context the migration context containing the data to transform
    /// @throws MigrationException if the migration fails
    void migrate(MigrationContext context);

    /// Creates a simple migration using a lambda expression.
    ///
    /// This factory method provides a concise way to define migrations inline:
    ///
    /// ```java
    /// Migration migration = Migration.of(2, "Add timeout field", ctx -> {
    ///     ctx.data().putIfAbsent("timeout", 30);
    /// });
    /// ```
    ///
    /// @param targetVersion the version this migration upgrades to
    /// @param description a description of what the migration does
    /// @param migrationAction the migration logic
    /// @return a new Migration instance
    static Migration of(int targetVersion, String description, MigrationAction migrationAction) {
        return new Migration() {
            @Override
            public int targetVersion() {
                return targetVersion;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public void migrate(MigrationContext context) {
                migrationAction.migrate(context);
            }
        };
    }

    /// Creates a migration with explicit source and target versions.
    ///
    /// Use this when the source version is not simply `targetVersion - 1`:
    ///
    /// ```java
    /// // Skip version 3, migrate directly from 2 to 4
    /// Migration migration = Migration.of(2, 4, "Consolidate changes from v3 and v4", ctx -> {
    ///     // Combined migration logic
    /// });
    /// ```
    ///
    /// @param sourceVersion the version this migration upgrades from
    /// @param targetVersion the version this migration upgrades to
    /// @param description a description of what the migration does
    /// @param migrationAction the migration logic
    /// @return a new Migration instance
    static Migration of(int sourceVersion, int targetVersion, String description, MigrationAction migrationAction) {
        return new Migration() {
            @Override
            public int sourceVersion() {
                return sourceVersion;
            }

            @Override
            public int targetVersion() {
                return targetVersion;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public void migrate(MigrationContext context) {
                migrationAction.migrate(context);
            }
        };
    }

    /// Functional interface for migration actions.
    ///
    /// This interface allows migrations to be defined using lambda expressions
    /// with the [Migration#of] factory methods.
    @FunctionalInterface
    interface MigrationAction {

        /// Performs the migration.
        ///
        /// @param context the migration context
        /// @throws MigrationException if the migration fails
        void migrate(MigrationContext context);
    }
}
