package net.blockhost.commons.config.migration;

import java.io.Serial;

/// Exception thrown when a configuration migration fails.
///
/// This exception is used throughout the migration system to indicate various
/// failure conditions, including:
///
/// - **YAML parsing errors**: When a configuration file contains invalid YAML syntax
/// - **Version errors**: When the version field is missing, invalid, or out of range
/// - **Migration errors**: When a migration fails to transform the data correctly
/// - **I/O errors**: When reading or writing configuration files fails
///
/// ## Exception Chaining
///
/// `MigrationException` supports exception chaining to preserve the original cause.
/// Always include the original exception when wrapping lower-level errors:
///
/// ```java
/// try {
///     // Some operation that might fail
/// } catch (IOException e) {
///     throw new MigrationException("Failed to read config file", e);
/// }
/// ```
///
/// ## Usage in Migrations
///
/// When implementing a [Migration], throw this exception to abort the migration
/// process and trigger rollback (if enabled):
///
/// ```java
/// @Override
/// public void migrate(MigrationContext context) {
///     Object oldValue = context.data().get("oldField");
///     if (oldValue == null) {
///         throw new MigrationException("Required field 'oldField' is missing");
///     }
///     // Continue with migration...
/// }
/// ```
///
/// @see Migration
/// @see MigrationExecutor
/// @see MigrationResult
public class MigrationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Creates a new migration exception with the specified message.
    ///
    /// @param message the detail message describing the failure
    public MigrationException(String message) {
        super(message);
    }

    /// Creates a new migration exception with the specified message and cause.
    ///
    /// @param message the detail message describing the failure
    /// @param cause the underlying cause of the failure
    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    /// Creates a new migration exception with the specified cause.
    ///
    /// The detail message is set to the cause's message.
    ///
    /// @param cause the underlying cause of the failure
    public MigrationException(Throwable cause) {
        super(cause);
    }

    /// Creates a migration exception indicating a version mismatch.
    ///
    /// @param currentVersion the current version found in the config
    /// @param expectedVersion the version that was expected
    /// @return a new MigrationException with a descriptive message
    public static MigrationException versionMismatch(int currentVersion, int expectedVersion) {
        return new MigrationException(
                "Version mismatch: found version %d, expected version %d".formatted(currentVersion, expectedVersion));
    }

    /// Creates a migration exception indicating a missing migration.
    ///
    /// @param fromVersion the source version
    /// @param toVersion the target version
    /// @return a new MigrationException with a descriptive message
    public static MigrationException missingMigration(int fromVersion, int toVersion) {
        return new MigrationException(
                "No migration registered for version %d to %d".formatted(fromVersion, toVersion));
    }

    /// Creates a migration exception indicating an invalid version range.
    ///
    /// @param fromVersion the source version
    /// @param toVersion the target version
    /// @return a new MigrationException with a descriptive message
    public static MigrationException invalidVersionRange(int fromVersion, int toVersion) {
        return new MigrationException(
                "Invalid version range: cannot migrate from version %d to version %d".formatted(fromVersion, toVersion));
    }

    /// Creates a migration exception indicating a migration failure at a specific version.
    ///
    /// @param version the version where migration failed
    /// @param cause the underlying cause
    /// @return a new MigrationException with a descriptive message
    public static MigrationException migrationFailed(int version, Throwable cause) {
        return new MigrationException("Migration to version %d failed".formatted(version), cause);
    }
}
