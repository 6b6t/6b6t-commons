package net.blockhost.commons.config.migration;

import de.exlll.configlib.Configuration;
import net.blockhost.commons.config.VersionAwareConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMigratorSubstitutionTest {

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
    void migrateAndLoad_withSubstitution_resolvesEnvAndSysPatterns() throws Exception {
        String osName = System.getProperty("os.name");

        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: ${sys:os.name}
                port: 3306
                label: static-value
                """);

        ConfigMigrator migrator =
                ConfigMigrator.builder().enableSubstitution(true).build();

        TestConfig config = migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        assertEquals(osName, config.hostname);
        assertEquals(3306, config.port);
        assertEquals("static-value", config.label);
    }

    @Test
    void migrateAndLoad_withSubstitution_doesNotWriteSubstitutedValuesToDisk() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: ${sys:os.name}
                port: 3306
                label: keep-this
                """);

        ConfigMigrator migrator =
                ConfigMigrator.builder().enableSubstitution(true).build();

        migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        // Verify the file on disk still has the unsubstituted pattern
        String fileContent = Files.readString(configFile);
        assertTrue(
                fileContent.contains("${sys:os.name}"),
                "File on disk should contain the unsubstituted pattern, but was:\n" + fileContent);
    }

    @Test
    void migrateAndLoad_withoutSubstitution_leavesPatterns() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: ${sys:os.name}
                port: 3306
                label: test
                """);

        ConfigMigrator migrator =
                ConfigMigrator.builder().enableSubstitution(false).build();

        TestConfig config = migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        // Without substitution enabled, the raw pattern is preserved in the loaded config
        assertEquals("${sys:os.name}", config.hostname);
    }

    @Test
    void migrateAndLoad_withSubstitutionAndMigration_appliesBoth() throws Exception {
        String osName = System.getProperty("os.name");

        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: ${sys:os.name}
                port: 3306
                label: original
                """);

        ConfigMigrator migrator = ConfigMigrator.builder()
                .enableSubstitution(true)
                .register(Migration.of(2, "Add timeout field", ctx -> {
                    ctx.setDefault("timeout", 30);
                }))
                .build();

        MigratedTestConfig config = migrator.migrateAndLoad(configFile, MigratedTestConfig.class, 2);

        // Substitution applied
        assertEquals(osName, config.hostname);
        // Migration applied
        assertEquals(30, config.timeout);
        assertEquals(2, config.version());
    }

    @Test
    void migrateAndLoad_withSubstitutionAndMigration_diskHasUnsubstitutedMigratedData() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: ${sys:os.name}
                port: 3306
                label: original
                """);

        ConfigMigrator migrator = ConfigMigrator.builder()
                .enableSubstitution(true)
                .register(Migration.of(2, "Add timeout field", ctx -> {
                    ctx.setDefault("timeout", 30);
                }))
                .build();

        migrator.migrateAndLoad(configFile, MigratedTestConfig.class, 2);

        // Verify disk state: migrated but NOT substituted
        String fileContent = Files.readString(configFile);
        assertTrue(fileContent.contains("${sys:os.name}"), "File on disk should still contain the pattern");
        assertTrue(
                fileContent.contains("version: 2") || fileContent.contains("version:2"),
                "File on disk should have the migrated version");
    }

    @Test
    void migrateAndLoad_withSubstitution_unresolvablePatternsPreserved() throws Exception {
        Path configFile = tempDir.resolve("config.yml");
        Files.writeString(configFile, """
                version: 1
                hostname: ${env:VERY_UNLIKELY_TO_EXIST_VARIABLE_XYZ_12345}
                port: 3306
                label: test
                """);

        ConfigMigrator migrator =
                ConfigMigrator.builder().enableSubstitution(true).build();

        TestConfig config = migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        // Unresolvable env vars are left as-is
        assertEquals("${env:VERY_UNLIKELY_TO_EXIST_VARIABLE_XYZ_12345}", config.hostname);
    }

    @Test
    void migrateAndLoad_newFile_withSubstitution_createsDefaults() {
        Path configFile = tempDir.resolve("nonexistent.yml");

        ConfigMigrator migrator =
                ConfigMigrator.builder().enableSubstitution(true).build();

        // Should create with defaults without error
        TestConfig config = migrator.migrateAndLoad(configFile, TestConfig.class, 1);

        assertEquals("localhost", config.hostname);
        assertEquals(3306, config.port);
    }
}
