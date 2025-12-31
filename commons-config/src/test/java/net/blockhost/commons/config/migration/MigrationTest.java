package net.blockhost.commons.config.migration;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MigrationTest {

    @Test
    void of_withTargetVersion_createsValidMigration() {
        Migration migration = Migration.of(3, "Test migration", ctx -> {
            ctx.data().put("migrated", true);
        });

        assertEquals(3, migration.targetVersion());
        assertEquals(2, migration.sourceVersion());
        assertEquals("Test migration", migration.description());
    }

    @Test
    void of_withSourceAndTargetVersion_createsValidMigration() {
        Migration migration = Migration.of(1, 5, "Skip versions", ctx -> {});

        assertEquals(1, migration.sourceVersion());
        assertEquals(5, migration.targetVersion());
        assertEquals("Skip versions", migration.description());
    }

    @Test
    void migrate_executesAction() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", 1);
        MigrationContext context = MigrationContext.ofData(data, 1, 2);

        Migration migration = Migration.of(2, "Add field", ctx -> {
            ctx.data().put("newField", "value");
        });

        migration.migrate(context);

        assertEquals("value", data.get("newField"));
    }

    @Test
    void defaultSourceVersion_isTargetMinusOne() {
        Migration migration = Migration.of(5, "Test", ctx -> {});

        assertEquals(4, migration.sourceVersion());
    }

    @Test
    void defaultDescription_generatesMessage() {
        Migration migration = new Migration() {
            @Override
            public int targetVersion() {
                return 3;
            }

            @Override
            public void migrate(MigrationContext context) {}
        };

        assertEquals("Migrate from version 2 to version 3", migration.description());
    }

    @Test
    void functionalInterface_worksWithLambda() {
        Migration migration = context -> context.data().put("test", true);

        // Default implementations should work
        assertEquals(0, migration.sourceVersion());
        assertEquals(1, migration.targetVersion());

        Map<String, Object> data = new LinkedHashMap<>();
        MigrationContext context = MigrationContext.ofData(data, 0, 1);
        migration.migrate(context);

        assertTrue((Boolean) data.get("test"));
    }

    @Test
    void customMigration_overridesAllMethods() {
        Migration migration = new Migration() {
            @Override
            public int sourceVersion() {
                return 10;
            }

            @Override
            public int targetVersion() {
                return 15;
            }

            @Override
            public String description() {
                return "Custom description";
            }

            @Override
            public void migrate(MigrationContext context) {
                context.data().put("custom", true);
            }
        };

        assertEquals(10, migration.sourceVersion());
        assertEquals(15, migration.targetVersion());
        assertEquals("Custom description", migration.description());
    }

    @Test
    void of_migrationActionIsExecuted() {
        boolean[] executed = {false};

        Migration migration = Migration.of(2, "Test", ctx -> {
            executed[0] = true;
        });

        Map<String, Object> data = new LinkedHashMap<>();
        MigrationContext context = MigrationContext.ofData(data, 1, 2);
        migration.migrate(context);

        assertTrue(executed[0]);
    }

    @Test
    void migrate_canThrowMigrationException() {
        Migration migration = Migration.of(2, "Failing migration", ctx -> {
            throw new MigrationException("Expected failure");
        });

        Map<String, Object> data = new LinkedHashMap<>();
        MigrationContext context = MigrationContext.ofData(data, 1, 2);

        MigrationException ex = assertThrows(MigrationException.class, () -> {
            migration.migrate(context);
        });

        assertEquals("Expected failure", ex.getMessage());
    }

    @Test
    void migrate_accessesContextData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("oldField", "oldValue");
        MigrationContext context = MigrationContext.ofData(data, 1, 2);

        Migration migration = Migration.of(2, "Rename field", ctx -> {
            ctx.rename("oldField", "newField");
        });

        migration.migrate(context);

        assertFalse(data.containsKey("oldField"));
        assertEquals("oldValue", data.get("newField"));
    }
}
