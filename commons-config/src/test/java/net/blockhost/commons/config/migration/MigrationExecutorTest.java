package net.blockhost.commons.config.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MigrationExecutorTest {

    private MigrationRegistry registry;
    private MigrationExecutor executor;
    private Map<String, Object> data;

    @BeforeEach
    void setUp() {
        registry = MigrationRegistry.create();
        executor = MigrationExecutor.create(registry);
        data = new LinkedHashMap<>();
        data.put("version", 1);
        data.put("name", "test");
    }

    @Test
    void execute_noMigrationNeeded_returnsSuccess() {
        MigrationResult result = executor.execute(data, 1, 1);

        assertTrue(result.isSuccess());
        assertEquals(1, result.fromVersion());
        assertEquals(1, result.toVersion());
        assertTrue(result.steps().isEmpty());
    }

    @Test
    void execute_appliesSingleMigration() {
        registry.register(Migration.of(2, "Add field", ctx -> {
            ctx.data().put("newField", "value");
        }));

        MigrationResult result = executor.execute(data, 1, 2);

        assertTrue(result.isSuccess());
        assertEquals(1, result.fromVersion());
        assertEquals(2, result.toVersion());
        assertEquals(1, result.steps().size());
        assertEquals("value", data.get("newField"));
        assertEquals(2, data.get("version"));
    }

    @Test
    void execute_appliesMultipleMigrations() {
        registry.register(Migration.of(2, "Add field1", ctx -> {
            ctx.data().put("field1", "v1");
        }));
        registry.register(Migration.of(3, "Add field2", ctx -> {
            ctx.data().put("field2", "v2");
        }));
        registry.register(Migration.of(4, "Add field3", ctx -> {
            ctx.data().put("field3", "v3");
        }));

        MigrationResult result = executor.execute(data, 1, 4);

        assertTrue(result.isSuccess());
        assertEquals(3, result.steps().size());
        assertEquals("v1", data.get("field1"));
        assertEquals("v2", data.get("field2"));
        assertEquals("v3", data.get("field3"));
        assertEquals(4, data.get("version"));
    }

    @Test
    void execute_migrationsInCorrectOrder() {
        List<Integer> order = new ArrayList<>();

        registry.register(Migration.of(2, "M2", ctx -> order.add(2)));
        registry.register(Migration.of(3, "M3", ctx -> order.add(3)));
        registry.register(Migration.of(4, "M4", ctx -> order.add(4)));

        executor.execute(data, 1, 4);

        assertEquals(List.of(2, 3, 4), order);
    }

    @Test
    void execute_missingMigration_strictMode_fails() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(4, "M4", ctx -> {}));
        // Missing version 3

        MigrationResult result = executor.execute(data, 1, 4);

        assertFalse(result.isSuccess());
        assertTrue(result.error().isPresent());
        assertTrue(result.error().get().getMessage().contains("Missing"));
    }

    @Test
    void execute_missingMigration_lenientMode_skips() {
        registry.register(Migration.of(2, "M2", ctx -> ctx.data().put("m2", true)));
        registry.register(Migration.of(4, "M4", ctx -> ctx.data().put("m4", true)));
        // Missing version 3

        executor.strictMode(false);
        MigrationResult result = executor.execute(data, 1, 4);

        assertTrue(result.isSuccess());
        assertEquals(true, data.get("m2"));
        assertEquals(true, data.get("m4"));
    }

    @Test
    void execute_migrationThrowsException_fails() {
        registry.register(Migration.of(2, "Failing migration", ctx -> {
            throw new MigrationException("Test failure");
        }));

        MigrationResult result = executor.execute(data, 1, 2);

        assertFalse(result.isSuccess());
        assertTrue(result.error().isPresent());
        assertTrue(result.error().get().getMessage().contains("Test failure"));
    }

    @Test
    void execute_migrationThrowsRuntimeException_wrapsIt() {
        registry.register(Migration.of(2, "Failing migration", ctx -> {
            throw new RuntimeException("Unexpected error");
        }));

        MigrationResult result = executor.execute(data, 1, 2);

        assertFalse(result.isSuccess());
        assertTrue(result.error().isPresent());
        assertNotNull(result.error().get().getCause());
    }

    @Test
    void execute_partialFailure_recordsCompletedSteps() {
        registry.register(Migration.of(2, "M2", ctx -> ctx.data().put("m2", true)));
        registry.register(Migration.of(3, "M3", ctx -> {
            throw new MigrationException("Failed");
        }));

        MigrationResult result = executor.execute(data, 1, 3);

        assertFalse(result.isSuccess());
        assertEquals(1, result.steps().size());
        assertEquals(2, result.steps().get(0).toVersion());
    }

    @Test
    void execute_invalidVersionRange_fails() {
        MigrationResult result = executor.execute(data, 5, 3);

        assertFalse(result.isSuccess());
        assertTrue(result.error().isPresent());
        assertTrue(result.error().get().getMessage().contains("Invalid version range"));
    }

    @Test
    void execute_dryRun_doesNotModifyData() {
        registry.register(Migration.of(2, "Add field", ctx -> {
            ctx.data().put("newField", "value");
        }));

        Map<String, Object> originalData = new LinkedHashMap<>(data);
        executor.dryRun(true);
        MigrationResult result = executor.execute(data, 1, 2);

        assertTrue(result.isSuccess());
        // Original data should be unchanged
        assertEquals(originalData.get("version"), data.get("version"));
        assertFalse(data.containsKey("newField"));
    }

    @Test
    void execute_beforeMigrationCallback_invoked() {
        List<String> callbacks = new ArrayList<>();
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        executor.beforeMigration(m -> callbacks.add("before-" + m.targetVersion()));
        executor.execute(data, 1, 3);

        assertEquals(List.of("before-2", "before-3"), callbacks);
    }

    @Test
    void execute_afterMigrationCallback_invoked() {
        List<String> callbacks = new ArrayList<>();
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        executor.afterMigration((m, ctx) -> callbacks.add("after-" + m.targetVersion()));
        executor.execute(data, 1, 3);

        assertEquals(List.of("after-2", "after-3"), callbacks);
    }

    @Test
    void execute_errorCallback_invokedOnFailure() {
        List<MigrationException> errors = new ArrayList<>();
        registry.register(Migration.of(2, "Failing", ctx -> {
            throw new MigrationException("Test error");
        }));

        executor.onError(errors::add);
        executor.execute(data, 1, 2);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().contains("Test error"));
    }

    @Test
    void execute_withFilePath_passesToContext() {
        Path testPath = Path.of("test/config.yml");
        List<Path> capturedPaths = new ArrayList<>();

        registry.register(Migration.of(2, "M2", ctx -> {
            capturedPaths.add(ctx.filePath());
        }));

        executor.execute(testPath, data, 1, 2);

        assertEquals(1, capturedPaths.size());
        assertEquals(testPath, capturedPaths.get(0));
    }

    @Test
    void canMigrate_complete_returnsTrue() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        assertTrue(executor.canMigrate(1, 3));
    }

    @Test
    void canMigrate_incomplete_returnsFalse() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(4, "M4", ctx -> {}));

        assertFalse(executor.canMigrate(1, 4));
    }

    @Test
    void canMigrate_sameVersion_returnsTrue() {
        assertTrue(executor.canMigrate(3, 3));
    }

    @Test
    void execute_stepDurationTracked() {
        registry.register(Migration.of(2, "M2", ctx -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        MigrationResult result = executor.execute(data, 1, 2);

        assertTrue(result.isSuccess());
        assertFalse(result.steps().isEmpty());
        assertTrue(result.steps().get(0).duration().toMillis() >= 10);
    }

    @Test
    void execute_totalDurationTracked() {
        registry.register(Migration.of(2, "M2", ctx -> {}));

        MigrationResult result = executor.execute(data, 1, 2);

        assertTrue(result.isSuccess());
        assertNotNull(result.duration());
    }

    @Test
    void registry_returnsConfiguredRegistry() {
        assertSame(registry, executor.registry());
    }
}
