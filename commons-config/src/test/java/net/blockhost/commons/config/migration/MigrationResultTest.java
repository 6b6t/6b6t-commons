package net.blockhost.commons.config.migration;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MigrationResultTest {

    @Test
    void success_createsSuccessfulResult() {
        Map<String, Object> data = Map.of("version", 3);
        List<MigrationResult.MigrationStep> steps = List.of(
                new MigrationResult.MigrationStep(1, 2, "M2", Duration.ofMillis(10)),
                new MigrationResult.MigrationStep(2, 3, "M3", Duration.ofMillis(15)));

        MigrationResult result = MigrationResult.success(1, 3, steps, Duration.ofMillis(25), data);

        assertTrue(result.isSuccess());
        assertEquals(1, result.fromVersion());
        assertEquals(3, result.toVersion());
        assertEquals(2, result.steps().size());
        assertEquals(Duration.ofMillis(25), result.duration());
        assertSame(data, result.data());
        assertTrue(result.error().isEmpty());
    }

    @Test
    void noMigrationNeeded_createsSuccessWithNoSteps() {
        Map<String, Object> data = Map.of("version", 5);

        MigrationResult result = MigrationResult.noMigrationNeeded(5, data);

        assertTrue(result.isSuccess());
        assertEquals(5, result.fromVersion());
        assertEquals(5, result.toVersion());
        assertTrue(result.steps().isEmpty());
        assertEquals(Duration.ZERO, result.duration());
        assertTrue(result.error().isEmpty());
    }

    @Test
    void failure_createsFailedResult() {
        Map<String, Object> data = Map.of("version", 2);
        List<MigrationResult.MigrationStep> stepsCompleted =
                List.of(new MigrationResult.MigrationStep(1, 2, "M2", Duration.ofMillis(10)));
        MigrationException error = new MigrationException("Test failure");

        MigrationResult result = MigrationResult.failure(1, 3, stepsCompleted, Duration.ofMillis(20), data, error);

        assertFalse(result.isSuccess());
        assertEquals(1, result.fromVersion());
        assertEquals(3, result.toVersion());
        assertEquals(1, result.steps().size());
        assertTrue(result.error().isPresent());
        assertSame(error, result.error().get());
    }

    @Test
    void migrationStep_from_createFromMigrationAndTiming() {
        Migration migration = Migration.of(2, "Test migration", ctx -> {});
        Instant start = Instant.now();
        Instant end = start.plusMillis(50);

        MigrationResult.MigrationStep step = MigrationResult.MigrationStep.from(migration, start, end);

        assertEquals(1, step.fromVersion());
        assertEquals(2, step.toVersion());
        assertEquals("Test migration", step.description());
        assertEquals(Duration.ofMillis(50), step.duration());
    }

    @Test
    void builder_createsSuccessfulResult() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", 1);

        MigrationResult.Builder builder = MigrationResult.builder(1, data);
        Migration m2 = Migration.of(2, "M2", ctx -> {});
        Migration m3 = Migration.of(3, "M3", ctx -> {});

        builder.startStep();
        builder.completeStep(m2);
        builder.startStep();
        builder.completeStep(m3);

        MigrationResult result = builder.build(3);

        assertTrue(result.isSuccess());
        assertEquals(1, result.fromVersion());
        assertEquals(3, result.toVersion());
        assertEquals(2, result.steps().size());
    }

    @Test
    void builder_createsFailedResult() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", 1);

        MigrationResult.Builder builder = MigrationResult.builder(1, data);
        Migration m2 = Migration.of(2, "M2", ctx -> {});

        builder.startStep();
        builder.completeStep(m2);

        MigrationException error = new MigrationException("Failed at v3");
        MigrationResult result = builder.fail(3, error);

        assertFalse(result.isSuccess());
        assertEquals(1, result.fromVersion());
        assertEquals(3, result.toVersion());
        assertEquals(1, result.steps().size());
        assertTrue(result.error().isPresent());
        assertEquals("Failed at v3", result.error().get().getMessage());
    }

    @Test
    void builder_noMigrationNeeded() {
        Map<String, Object> data = Map.of("version", 5);

        MigrationResult.Builder builder = MigrationResult.builder(5, data);
        MigrationResult result = builder.noMigrationNeeded();

        assertTrue(result.isSuccess());
        assertEquals(5, result.fromVersion());
        assertEquals(5, result.toVersion());
        assertTrue(result.steps().isEmpty());
    }

    @Test
    void steps_areImmutable() {
        Map<String, Object> data = Map.of("version", 2);
        List<MigrationResult.MigrationStep> steps =
                List.of(new MigrationResult.MigrationStep(1, 2, "M2", Duration.ofMillis(10)));

        MigrationResult result = MigrationResult.success(1, 2, steps, Duration.ofMillis(10), data);

        assertThrows(UnsupportedOperationException.class, () -> {
            result.steps().add(new MigrationResult.MigrationStep(2, 3, "M3", Duration.ZERO));
        });
    }

    @Test
    void successRecord_implementsInterface() {
        MigrationResult.Success success = new MigrationResult.Success(1, 2, List.of(), Duration.ZERO, Map.of());

        assertTrue(success.isSuccess());
        assertTrue(success.error().isEmpty());
        assertEquals(1, success.fromVersion());
        assertEquals(2, success.toVersion());
    }

    @Test
    void failureRecord_implementsInterface() {
        MigrationException error = new MigrationException("Test");
        MigrationResult.Failure failure = new MigrationResult.Failure(1, 2, List.of(), Duration.ZERO, Map.of(), error);

        assertFalse(failure.isSuccess());
        assertTrue(failure.error().isPresent());
        assertSame(error, failure.cause());
    }

    @Test
    void migrationStep_recordComponents() {
        MigrationResult.MigrationStep step =
                new MigrationResult.MigrationStep(1, 2, "Test step", Duration.ofMillis(100));

        assertEquals(1, step.fromVersion());
        assertEquals(2, step.toVersion());
        assertEquals("Test step", step.description());
        assertEquals(Duration.ofMillis(100), step.duration());
    }
}
