package net.blockhost.commons.config.migration;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/// Encapsulates the outcome of a configuration migration operation.
///
/// A migration result contains information about whether the migration succeeded,
/// which versions were involved, timing information, and details about each
/// individual migration step that was applied.
///
/// ## Success vs Failure
///
/// Use [#isSuccess()] to check if the migration completed successfully:
///
/// ```java
/// MigrationResult result = migrator.migrate(configPath, 5);
/// if (result.isSuccess()) {
///     System.out.println("Migrated to version " + result.toVersion());
/// } else {
///     System.err.println("Migration failed: " + result.error().getMessage());
/// }
/// ```
///
/// ## Migration Steps
///
/// For successful migrations, [#steps()] provides details about each migration
/// that was applied:
///
/// ```java
/// for (MigrationStep step : result.steps()) {
///     System.out.printf("Applied migration %d -> %d: %s%n",
///         step.fromVersion(), step.toVersion(), step.description());
/// }
/// ```
///
/// @see Migration
/// @see MigrationExecutor
/// @see ConfigMigrator
public sealed interface MigrationResult {

    /// Returns whether the migration completed successfully.
    ///
    /// @return true if all migrations were applied successfully
    boolean isSuccess();

    /// Returns the version the data was at before migration.
    ///
    /// @return the starting version
    int fromVersion();

    /// Returns the version the data is at after migration.
    ///
    /// For successful migrations, this is the target version.
    /// For failed migrations, this is the last successfully applied version.
    ///
    /// @return the ending version
    int toVersion();

    /// Returns the list of migration steps that were applied.
    ///
    /// For failed migrations, this includes only the steps that completed
    /// successfully before the failure.
    ///
    /// @return an unmodifiable list of migration steps
    List<MigrationStep> steps();

    /// Returns the total duration of the migration operation.
    ///
    /// @return the duration from start to completion
    Duration duration();

    /// Returns the migrated data.
    ///
    /// For successful migrations, this is the fully transformed data.
    /// For failed migrations, this may be partial or the original data.
    ///
    /// @return the data map
    Map<String, Object> data();

    /// Returns the error if the migration failed.
    ///
    /// @return an Optional containing the exception if failed, empty if successful
    Optional<MigrationException> error();

    /// Creates a successful migration result.
    ///
    /// @param fromVersion the starting version
    /// @param toVersion the target version
    /// @param steps the migration steps applied
    /// @param duration the total duration
    /// @param data the migrated data
    /// @return a successful result
    static MigrationResult success(
            int fromVersion, int toVersion, List<MigrationStep> steps, Duration duration, Map<String, Object> data) {
        Objects.requireNonNull(steps, "steps");
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(data, "data");
        return new Success(fromVersion, toVersion, List.copyOf(steps), duration, Map.copyOf(data));
    }

    /// Creates a result indicating no migration was needed.
    ///
    /// @param version the current version (which equals the target)
    /// @param data the unchanged data
    /// @return a success result with no steps
    static MigrationResult noMigrationNeeded(int version, Map<String, Object> data) {
        Objects.requireNonNull(data, "data");
        return new Success(version, version, List.of(), Duration.ZERO, Map.copyOf(data));
    }

    /// Creates a failed migration result.
    ///
    /// @param fromVersion the starting version
    /// @param failedAtVersion the version where migration failed
    /// @param stepsCompleted the steps that completed before failure
    /// @param duration the duration until failure
    /// @param data the data state at failure
    /// @param error the exception that caused the failure
    /// @return a failed result
    static MigrationResult failure(
            int fromVersion,
            int failedAtVersion,
            List<MigrationStep> stepsCompleted,
            Duration duration,
            Map<String, Object> data,
            MigrationException error) {
        Objects.requireNonNull(stepsCompleted, "stepsCompleted");
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(error, "error");
        return new Failure(
                fromVersion, failedAtVersion, List.copyOf(stepsCompleted), duration, Map.copyOf(data), error);
    }

    /// Represents a successful migration result.
    record Success(
            int fromVersion, int toVersion, List<MigrationStep> steps, Duration duration, Map<String, Object> data)
            implements MigrationResult {

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public Optional<MigrationException> error() {
            return Optional.empty();
        }
    }

    /// Represents a failed migration result.
    record Failure(
            int fromVersion,
            int toVersion,
            List<MigrationStep> steps,
            Duration duration,
            Map<String, Object> data,
            MigrationException cause)
            implements MigrationResult {

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public Optional<MigrationException> error() {
            return Optional.of(cause);
        }
    }

    /// Represents a single migration step that was applied.
    ///
    /// @param fromVersion the version before this step
    /// @param toVersion the version after this step
    /// @param description the description of what the migration did
    /// @param duration how long this step took
    record MigrationStep(int fromVersion, int toVersion, String description, Duration duration) {

        /// Creates a migration step from a migration and timing information.
        ///
        /// @param migration the migration that was applied
        /// @param start the start time
        /// @param end the end time
        /// @return a new migration step
        public static MigrationStep from(Migration migration, Instant start, Instant end) {
            Objects.requireNonNull(migration, "migration");
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(end, "end");
            return new MigrationStep(
                    migration.sourceVersion(),
                    migration.targetVersion(),
                    migration.description(),
                    Duration.between(start, end));
        }
    }

    /// Builder for creating migration results incrementally.
    ///
    /// This is useful when executing multiple migrations and tracking progress:
    ///
    /// ```java
    /// MigrationResult.Builder builder = MigrationResult.builder(1, data);
    /// for (Migration migration : migrations) {
    ///     try {
    ///         builder.startStep();
    ///         migration.migrate(context);
    ///         builder.completeStep(migration);
    ///     } catch (Exception e) {
    ///         return builder.fail(migration.targetVersion(), e);
    ///     }
    /// }
    /// return builder.build(targetVersion);
    /// ```
    final class Builder {

        private final int fromVersion;
        private final Map<String, Object> data;
        private final java.util.ArrayList<MigrationStep> steps;
        private final Instant startTime;
        private @Nullable Instant stepStartTime;

        private Builder(int fromVersion, Map<String, Object> data) {
            this.fromVersion = fromVersion;
            this.data = data;
            this.steps = new java.util.ArrayList<>();
            this.startTime = Instant.now();
        }

        /// Marks the start of a migration step.
        public void startStep() {
            this.stepStartTime = Instant.now();
        }

        /// Records the completion of a migration step.
        ///
        /// @param migration the migration that was applied
        public void completeStep(Migration migration) {
            Objects.requireNonNull(migration, "migration");
            Instant end = Instant.now();
            Instant start = stepStartTime != null ? stepStartTime : end;
            steps.add(MigrationStep.from(migration, start, end));
            stepStartTime = null;
        }

        /// Builds a successful result.
        ///
        /// @param toVersion the final version
        /// @return the successful result
        public MigrationResult build(int toVersion) {
            return success(fromVersion, toVersion, steps, Duration.between(startTime, Instant.now()), data);
        }

        /// Builds a failed result.
        ///
        /// @param failedAtVersion the version where migration failed
        /// @param cause the exception that caused the failure
        /// @return the failed result
        public MigrationResult fail(int failedAtVersion, MigrationException cause) {
            Objects.requireNonNull(cause, "cause");
            return failure(
                    fromVersion, failedAtVersion, steps, Duration.between(startTime, Instant.now()), data, cause);
        }

        /// Builds a result indicating no migration was needed.
        ///
        /// @return the no-op result
        public MigrationResult noMigrationNeeded() {
            return MigrationResult.noMigrationNeeded(fromVersion, data);
        }
    }

    /// Creates a new result builder.
    ///
    /// @param fromVersion the starting version
    /// @param data the data being migrated
    /// @return a new builder
    static Builder builder(int fromVersion, Map<String, Object> data) {
        Objects.requireNonNull(data, "data");
        return new Builder(fromVersion, data);
    }
}
