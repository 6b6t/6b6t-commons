package net.blockhost.commons.config.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MigrationRegistryTest {

    private MigrationRegistry registry;

    @BeforeEach
    void setUp() {
        registry = MigrationRegistry.create();
    }

    @Test
    void create_returnsEmptyRegistry() {
        assertTrue(registry.isEmpty());
        assertEquals(0, registry.size());
    }

    @Test
    void register_addsMigration() {
        Migration migration = Migration.of(2, "Test migration", ctx -> {});

        registry.register(migration);

        assertEquals(1, registry.size());
        assertTrue(registry.contains(2));
    }

    @Test
    void register_duplicateVersion_throwsException() {
        Migration first = Migration.of(2, "First", ctx -> {});
        Migration second = Migration.of(2, "Second", ctx -> {});

        registry.register(first);

        assertThrows(IllegalArgumentException.class, () -> registry.register(second));
    }

    @Test
    void registerOrReplace_replacesMigration() {
        Migration first = Migration.of(2, "First", ctx -> {});
        Migration second = Migration.of(2, "Second", ctx -> {});

        registry.register(first);
        Migration replaced = registry.registerOrReplace(second);

        assertSame(first, replaced);
        assertEquals("Second", registry.get(2).orElseThrow().description());
    }

    @Test
    void get_returnsMigration() {
        Migration migration = Migration.of(3, "Test", ctx -> {});
        registry.register(migration);

        Optional<Migration> found = registry.get(3);

        assertTrue(found.isPresent());
        assertSame(migration, found.get());
    }

    @Test
    void get_missingVersion_returnsEmpty() {
        Optional<Migration> found = registry.get(99);
        assertTrue(found.isEmpty());
    }

    @Test
    void remove_removesMigration() {
        Migration migration = Migration.of(2, "Test", ctx -> {});
        registry.register(migration);

        Migration removed = registry.remove(2);

        assertSame(migration, removed);
        assertFalse(registry.contains(2));
    }

    @Test
    void clear_removesAllMigrations() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        registry.clear();

        assertTrue(registry.isEmpty());
    }

    @Test
    void getAllMigrations_returnsSortedList() {
        registry.register(Migration.of(5, "M5", ctx -> {}));
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        List<Migration> all = registry.getAllMigrations();

        assertEquals(3, all.size());
        assertEquals(2, all.get(0).targetVersion());
        assertEquals(3, all.get(1).targetVersion());
        assertEquals(5, all.get(2).targetVersion());
    }

    @Test
    void getAllVersions_returnsSortedList() {
        registry.register(Migration.of(5, "M5", ctx -> {}));
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        List<Integer> versions = registry.getAllVersions();

        assertEquals(List.of(2, 3, 5), versions);
    }

    @Test
    void getHighestVersion_returnsMaxVersion() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(5, "M5", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        Optional<Integer> highest = registry.getHighestVersion();

        assertTrue(highest.isPresent());
        assertEquals(5, highest.get());
    }

    @Test
    void getHighestVersion_emptyRegistry_returnsEmpty() {
        Optional<Integer> highest = registry.getHighestVersion();
        assertTrue(highest.isEmpty());
    }

    @Test
    void getLowestVersion_returnsMinVersion() {
        registry.register(Migration.of(5, "M5", ctx -> {}));
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        Optional<Integer> lowest = registry.getLowestVersion();

        assertTrue(lowest.isPresent());
        assertEquals(2, lowest.get());
    }

    @Test
    void getMigrationsInRange_returnsCorrectMigrations() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));
        registry.register(Migration.of(4, "M4", ctx -> {}));
        registry.register(Migration.of(5, "M5", ctx -> {}));

        List<Migration> range = registry.getMigrationsInRange(1, 4);

        assertEquals(3, range.size());
        assertEquals(2, range.get(0).targetVersion());
        assertEquals(3, range.get(1).targetVersion());
        assertEquals(4, range.get(2).targetVersion());
    }

    @Test
    void getMigrationsInRange_sameVersion_returnsEmpty() {
        registry.register(Migration.of(2, "M2", ctx -> {}));

        List<Migration> range = registry.getMigrationsInRange(2, 2);

        assertTrue(range.isEmpty());
    }

    @Test
    void getMigrationsInRange_invalidRange_throwsException() {
        assertThrows(MigrationException.class, () -> registry.getMigrationsInRange(5, 3));
    }

    @Test
    void findMissingMigrations_returnsGaps() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(4, "M4", ctx -> {}));

        List<Integer> missing = registry.findMissingMigrations(1, 5);

        assertEquals(List.of(3, 5), missing);
    }

    @Test
    void findMissingMigrations_noGaps_returnsEmpty() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));
        registry.register(Migration.of(4, "M4", ctx -> {}));

        List<Integer> missing = registry.findMissingMigrations(1, 4);

        assertTrue(missing.isEmpty());
    }

    @Test
    void hasCompleteMigrationChain_withGaps_returnsFalse() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(4, "M4", ctx -> {}));

        assertFalse(registry.hasCompleteMigrationChain(1, 4));
    }

    @Test
    void hasCompleteMigrationChain_complete_returnsTrue() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        assertTrue(registry.hasCompleteMigrationChain(1, 3));
    }

    @Test
    void of_varargs_createsPopulatedRegistry() {
        Migration m2 = Migration.of(2, "M2", ctx -> {});
        Migration m3 = Migration.of(3, "M3", ctx -> {});

        MigrationRegistry populated = MigrationRegistry.of(m2, m3);

        assertEquals(2, populated.size());
        assertTrue(populated.contains(2));
        assertTrue(populated.contains(3));
    }

    @Test
    void registerAll_addsMultipleMigrations() {
        registry.registerAll(
                Migration.of(2, "M2", ctx -> {}), Migration.of(3, "M3", ctx -> {}), Migration.of(4, "M4", ctx -> {}));

        assertEquals(3, registry.size());
    }

    @Test
    void immutableCopy_returnsUnmodifiableCopy() {
        registry.register(Migration.of(2, "M2", ctx -> {}));

        MigrationRegistry copy = registry.immutableCopy();

        assertEquals(1, copy.size());
        // Original can still be modified
        registry.register(Migration.of(3, "M3", ctx -> {}));
        assertEquals(1, copy.size());
        assertEquals(2, registry.size());
    }

    @Test
    void toString_includesVersions() {
        registry.register(Migration.of(2, "M2", ctx -> {}));
        registry.register(Migration.of(3, "M3", ctx -> {}));

        String str = registry.toString();

        assertTrue(str.contains("2"));
        assertTrue(str.contains("3"));
    }

    @Test
    void toString_emptyRegistry() {
        String str = registry.toString();
        assertTrue(str.contains("empty"));
    }
}
