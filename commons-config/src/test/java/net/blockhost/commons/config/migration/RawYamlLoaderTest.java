package net.blockhost.commons.config.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RawYamlLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadFromString_parsesSimpleYaml() {
        String yaml = """
                name: test
                value: 42
                enabled: true
                """;

        Map<String, Object> data = RawYamlLoader.loadFromString(yaml);

        assertEquals("test", data.get("name"));
        assertEquals(42, data.get("value"));
        assertEquals(true, data.get("enabled"));
    }

    @Test
    void loadFromString_parsesNestedYaml() {
        String yaml = """
                database:
                  host: localhost
                  port: 3306
                """;

        Map<String, Object> data = RawYamlLoader.loadFromString(yaml);

        @SuppressWarnings("unchecked")
        Map<String, Object> database = (Map<String, Object>) data.get("database");
        assertNotNull(database);
        assertEquals("localhost", database.get("host"));
        assertEquals(3306, database.get("port"));
    }

    @Test
    void loadFromString_emptyString_returnsEmptyMap() {
        Map<String, Object> data = RawYamlLoader.loadFromString("");
        assertTrue(data.isEmpty());
    }

    @Test
    void loadFromString_blankString_returnsEmptyMap() {
        Map<String, Object> data = RawYamlLoader.loadFromString("   \n\n   ");
        assertTrue(data.isEmpty());
    }

    @Test
    void load_nonExistentFile_returnsEmptyMap() {
        Path nonExistent = tempDir.resolve("does-not-exist.yml");
        Map<String, Object> data = RawYamlLoader.load(nonExistent);
        assertTrue(data.isEmpty());
    }

    @Test
    void load_existingFile_parsesContent() throws Exception {
        Path file = tempDir.resolve("config.yml");
        Files.writeString(file, """
                version: 1
                name: test
                """);

        Map<String, Object> data = RawYamlLoader.load(file);

        assertEquals(1, data.get("version"));
        assertEquals("test", data.get("name"));
    }

    @Test
    void save_createsFileWithContent() {
        Path file = tempDir.resolve("output.yml");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", 2);
        data.put("name", "saved");

        RawYamlLoader.save(file, data);

        assertTrue(Files.exists(file));
        Map<String, Object> loaded = RawYamlLoader.load(file);
        assertEquals(2, loaded.get("version"));
        assertEquals("saved", loaded.get("name"));
    }

    @Test
    void save_createsParentDirectories() {
        Path file = tempDir.resolve("nested/deep/config.yml");
        Map<String, Object> data = Map.of("key", "value");

        RawYamlLoader.save(file, data);

        assertTrue(Files.exists(file));
    }

    @Test
    void saveToString_producesValidYaml() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "test");
        data.put("count", 10);

        String yaml = RawYamlLoader.saveToString(data);

        assertNotNull(yaml);
        assertTrue(yaml.contains("name: test"));
        assertTrue(yaml.contains("count: 10"));
    }

    @Test
    void extractVersion_returnsVersionNumber() {
        Map<String, Object> data = Map.of("version", 5, "name", "test");

        int version = RawYamlLoader.extractVersion(data);

        assertEquals(5, version);
    }

    @Test
    void extractVersion_noVersionField_returnsZero() {
        Map<String, Object> data = Map.of("name", "test");

        int version = RawYamlLoader.extractVersion(data);

        assertEquals(0, version);
    }

    @Test
    void extractVersion_stringVersion_parsesCorrectly() {
        Map<String, Object> data = Map.of("version", "3");

        int version = RawYamlLoader.extractVersion(data);

        assertEquals(3, version);
    }

    @Test
    void extractVersion_invalidStringVersion_throwsException() {
        Map<String, Object> data = Map.of("version", "invalid");

        assertThrows(MigrationException.class, () -> RawYamlLoader.extractVersion(data));
    }

    @Test
    void readVersion_fromFile() throws Exception {
        Path file = tempDir.resolve("versioned.yml");
        Files.writeString(file, "version: 7\nname: test\n");

        int version = RawYamlLoader.readVersion(file);

        assertEquals(7, version);
    }

    @Test
    void readVersion_nonExistentFile_returnsZero() {
        Path file = tempDir.resolve("missing.yml");

        int version = RawYamlLoader.readVersion(file);

        assertEquals(0, version);
    }

    @Test
    void loadFromString_parsesList() {
        String yaml = """
                items:
                  - first
                  - second
                  - third
                """;

        Map<String, Object> data = RawYamlLoader.loadFromString(yaml);

        @SuppressWarnings("unchecked")
        List<String> items = (List<String>) data.get("items");
        assertNotNull(items);
        assertEquals(3, items.size());
        assertEquals("first", items.get(0));
    }
}
