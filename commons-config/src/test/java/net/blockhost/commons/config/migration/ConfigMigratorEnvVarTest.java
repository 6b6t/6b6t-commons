package net.blockhost.commons.config.migration;

import de.exlll.configlib.Configuration;
import net.blockhost.commons.config.VersionAwareConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMigratorEnvVarTest {

    @TempDir
    Path tempDir;

    @Configuration
    static class TestConfig extends VersionAwareConfiguration {

        private String hostname = "localhost";
        private int port = 3306;
        private String label = "default";

        TestConfig() {
            super(1);
        }
    }

    @Configuration
    static class MigratedTestConfig extends VersionAwareConfiguration {

        private String hostname = "localhost";
        private int port = 3306;
        private String label = "default";
        private int timeout = 30;

        MigratedTestConfig() {
            super(2);
        }
    }

    @Test
    void migrateAndLoad_withEnvPrefix_preservesYamlValuesWhenNoMatchingEnvVar() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: from-yaml
                port: 3306
                label: static-value
                """);

        ConfigMigrator migrator =
                ConfigMigrator.builder().envPrefix("TEST_UNLIKELY_PREFIX_").build();

        TestConfig config = migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        // No matching env var, so YAML value is preserved
        assertEquals("from-yaml", config.hostname);
        assertEquals(3306, config.port);
        assertEquals("static-value", config.label);
    }

    @Test
    void migrateAndLoad_withEnvPrefix_doesNotWriteResolvedValuesToDisk() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: original-value
                port: 3306
                label: keep-this
                """);

        ConfigMigrator migrator =
                ConfigMigrator.builder().envPrefix("TEST_UNLIKELY_PREFIX_").build();

        migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        // Verify the file on disk still has the original value
        String fileContent = Files.readString(configFile);
        assertTrue(
                fileContent.contains("original-value"),
                "File on disk should contain the original value, but was:\n" + fileContent);
    }

    @Test
    void migrateAndLoad_withoutEnvPrefix_loadsNormally() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: yaml-host
                port: 3306
                label: test
                """);

        ConfigMigrator migrator = ConfigMigrator.builder().build();

        TestConfig config = migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        assertEquals("yaml-host", config.hostname);
    }

    @Test
    void migrateAndLoad_withEnvPrefixAndMigration_appliesBoth() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: yaml-host
                port: 3306
                label: original
                """);

        ConfigMigrator migrator = ConfigMigrator.builder()
                .envPrefix("TEST_UNLIKELY_PREFIX_")
                .register(Migration.of(2, "Add timeout field", ctx -> {
                    ctx.setDefault("timeout", 30);
                }))
                .build();

        MigratedTestConfig config = migrator.migrateAndLoad(configFile, MigratedTestConfig.class, 2);

        // YAML value preserved (no matching env var)
        assertEquals("yaml-host", config.hostname);
        // Migration applied
        assertEquals(30, config.timeout);
        assertEquals(2, config.version());
    }

    @Test
    void migrateAndLoad_withEnvPrefixAndMigration_diskHasOriginalData() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: yaml-host
                port: 3306
                label: original
                """);

        ConfigMigrator migrator = ConfigMigrator.builder()
                .envPrefix("TEST_UNLIKELY_PREFIX_")
                .register(Migration.of(2, "Add timeout field", ctx -> {
                    ctx.setDefault("timeout", 30);
                }))
                .build();

        migrator.migrateAndLoad(configFile, MigratedTestConfig.class, 2);

        // Verify disk state: migrated data preserved
        String fileContent = Files.readString(configFile);
        assertTrue(fileContent.contains("yaml-host"), "File on disk should still contain yaml-host");
        assertTrue(
                fileContent.contains("version: 2") || fileContent.contains("version:2"),
                "File on disk should have the migrated version");
    }

    @Test
    void migrateAndLoad_newFile_withEnvPrefix_createsDefaults() {
        Path configFile = tempDir.resolve("nonexistent.yml");

        ConfigMigrator migrator =
                ConfigMigrator.builder().envPrefix("TEST_UNLIKELY_PREFIX_").build();

        // Should create with defaults without error
        TestConfig config = migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        assertEquals("localhost", config.hostname);
        assertEquals(3306, config.port);
    }
}
