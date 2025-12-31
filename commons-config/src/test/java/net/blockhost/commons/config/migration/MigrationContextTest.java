package net.blockhost.commons.config.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MigrationContextTest {

    private Map<String, Object> data;
    private MigrationContext context;

    @BeforeEach
    void setUp() {
        data = new LinkedHashMap<>();
        data.put("version", 1);
        data.put("name", "test");
        data.put("count", 42);
        data.put("enabled", true);

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("host", "localhost");
        nested.put("port", 3306);
        data.put("database", nested);

        context = MigrationContext.of(Path.of("config.yml"), data, 1, 2);
    }

    @Test
    void of_createsContextWithCorrectValues() {
        assertEquals(Path.of("config.yml"), context.filePath());
        assertEquals(1, context.currentVersion());
        assertEquals(2, context.targetVersion());
        assertSame(data, context.data());
    }

    @Test
    void get_returnsTypedValue() {
        Optional<String> name = context.get("name", String.class);
        assertTrue(name.isPresent());
        assertEquals("test", name.get());
    }

    @Test
    void get_wrongType_returnsEmpty() {
        Optional<Integer> name = context.get("name", Integer.class);
        assertTrue(name.isEmpty());
    }

    @Test
    void get_missingKey_returnsEmpty() {
        Optional<String> missing = context.get("missing", String.class);
        assertTrue(missing.isEmpty());
    }

    @Test
    void getString_returnsValue() {
        Optional<String> name = context.getString("name");
        assertTrue(name.isPresent());
        assertEquals("test", name.get());
    }

    @Test
    void getInt_returnsValue() {
        Optional<Integer> count = context.getInt("count");
        assertTrue(count.isPresent());
        assertEquals(42, count.get());
    }

    @Test
    void getBoolean_returnsValue() {
        Optional<Boolean> enabled = context.getBoolean("enabled");
        assertTrue(enabled.isPresent());
        assertTrue(enabled.get());
    }

    @Test
    void getNestedMap_returnsMap() {
        Optional<Map<String, Object>> database = context.getNestedMap("database");
        assertTrue(database.isPresent());
        assertEquals("localhost", database.get().get("host"));
    }

    @Test
    void getNestedMap_notAMap_returnsEmpty() {
        Optional<Map<String, Object>> name = context.getNestedMap("name");
        assertTrue(name.isEmpty());
    }

    @Test
    void getOrCreateNestedMap_existingMap_returnsIt() {
        Map<String, Object> database = context.getOrCreateNestedMap("database");
        assertEquals("localhost", database.get("host"));
    }

    @Test
    void getOrCreateNestedMap_missingKey_createsNewMap() {
        Map<String, Object> newSection = context.getOrCreateNestedMap("newSection");
        assertNotNull(newSection);
        assertTrue(newSection.isEmpty());
        assertTrue(data.containsKey("newSection"));
    }

    @Test
    void getOrCreateNestedMap_notAMap_throwsException() {
        assertThrows(MigrationException.class, () -> context.getOrCreateNestedMap("name"));
    }

    @Test
    void getNestedValue_returnsValue() {
        Optional<String> host = context.getNestedValue("database", "host", String.class);
        assertTrue(host.isPresent());
        assertEquals("localhost", host.get());
    }

    @Test
    void rename_renamesKey() {
        boolean result = context.rename("name", "title");

        assertTrue(result);
        assertFalse(data.containsKey("name"));
        assertEquals("test", data.get("title"));
    }

    @Test
    void rename_missingKey_returnsFalse() {
        boolean result = context.rename("missing", "newName");

        assertFalse(result);
    }

    @Test
    void renameNested_renamesKeyInNestedMap() {
        boolean result = context.renameNested("database", "host", "hostname");

        assertTrue(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> database = (Map<String, Object>) data.get("database");
        assertFalse(database.containsKey("host"));
        assertEquals("localhost", database.get("hostname"));
    }

    @Test
    void setDefault_setsValueIfMissing() {
        boolean result = context.setDefault("timeout", 30);

        assertTrue(result);
        assertEquals(30, data.get("timeout"));
    }

    @Test
    void setDefault_doesNotOverwriteExisting() {
        boolean result = context.setDefault("name", "other");

        assertFalse(result);
        assertEquals("test", data.get("name"));
    }

    @Test
    void moveToNested_movesValueToNestedMap() {
        data.put("dbHost", "127.0.0.1");

        boolean result = context.moveToNested("dbHost", "connection", "host");

        assertTrue(result);
        assertFalse(data.containsKey("dbHost"));
        @SuppressWarnings("unchecked")
        Map<String, Object> connection = (Map<String, Object>) data.get("connection");
        assertEquals("127.0.0.1", connection.get("host"));
    }

    @Test
    void moveFromNested_movesValueToRoot() {
        boolean result = context.moveFromNested("database", "host", "dbHost");

        assertTrue(result);
        assertEquals("localhost", data.get("dbHost"));
        @SuppressWarnings("unchecked")
        Map<String, Object> database = (Map<String, Object>) data.get("database");
        assertFalse(database.containsKey("host"));
    }

    @Test
    void copyData_createsDeepCopy() {
        Map<String, Object> copy = context.copyData();

        assertNotSame(data, copy);
        assertEquals(data.get("name"), copy.get("name"));

        // Modify copy, original should be unchanged
        copy.put("name", "modified");
        assertEquals("test", data.get("name"));

        // Nested maps should also be copied
        @SuppressWarnings("unchecked")
        Map<String, Object> copiedDatabase = (Map<String, Object>) copy.get("database");
        copiedDatabase.put("host", "modified");
        @SuppressWarnings("unchecked")
        Map<String, Object> originalDatabase = (Map<String, Object>) data.get("database");
        assertEquals("localhost", originalDatabase.get("host"));
    }

    @Test
    void getList_returnsList() {
        data.put("items", List.of("a", "b", "c"));

        Optional<List<Object>> items = context.getList("items");

        assertTrue(items.isPresent());
        assertEquals(3, items.get().size());
    }

    @Test
    void ofData_createsContextWithoutFilePath() {
        MigrationContext ctx = MigrationContext.ofData(data, 1, 3);

        assertEquals(Path.of(""), ctx.filePath());
        assertEquals(1, ctx.currentVersion());
        assertEquals(3, ctx.targetVersion());
    }

    @Test
    void toString_includesRelevantInfo() {
        String str = context.toString();

        assertTrue(str.contains("config.yml"));
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
    }
}
