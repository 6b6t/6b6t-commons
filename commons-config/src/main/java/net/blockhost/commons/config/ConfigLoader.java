package net.blockhost.commons.config;

import de.exlll.configlib.DeserializationCoercionType;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import lombok.experimental.UtilityClass;
import net.blockhost.commons.core.io.AtomicFileWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/// Utility class for loading and saving configuration files using ConfigLib.
///
/// This class provides a simplified API for working with ConfigLib's YAML configurations,
/// with sensible defaults and common configuration patterns. All methods require explicit
/// properties, which can be obtained via [#defaultPropertiesBuilder()].
///
/// Example usage:
/// ```java
/// // Define your configuration class
/// @Configuration
/// public class MyConfig {
///     private String host = "localhost";
///     private int port = 3306;
/// }
///
/// // Load or create configuration
/// Path configPath = dataFolder.resolve("config.yml");
/// var properties = ConfigLoader.defaultPropertiesBuilder().build();
/// MyConfig config = ConfigLoader.loadOrCreate(configPath, MyConfig.class, properties);
///
/// // Load with env var resolution (e.g. MYPLUGIN_HOST overrides host)
/// var envProperties = ConfigLoader.defaultPropertiesBuilder("MYPLUGIN_").build();
/// MyConfig config = ConfigLoader.load(configPath, MyConfig.class, envProperties);
/// ```
///
/// @see de.exlll.configlib.Configuration
/// @see YamlConfigurations
@UtilityClass
public class ConfigLoader {

    /// Loads a configuration from the specified path, or creates it with default values if it doesn't exist.
    ///
    /// This method uses the `update` strategy, which:
    /// - Creates the file with defaults if it doesn't exist
    /// - Adds any new fields that are in the class but not in the file
    /// - Preserves existing values in the file
    /// - Removes fields that are no longer in the class
    public <T> T loadOrCreate(Path path, Class<T> configClass, YamlConfigurationProperties properties) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(properties, "properties");
        return YamlConfigurations.update(path, configClass, properties);
    }

    /// Saves a configuration to the specified path.
    ///
    /// This will overwrite any existing file at the path.
    public <T> void save(Path path, Class<T> configClass, T config, YamlConfigurationProperties properties) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(properties, "properties");
        YamlConfigurations.save(path, configClass, config, properties);
    }

    /// Loads a configuration from an existing file.
    ///
    /// Unlike [#loadOrCreate], this method requires the file to exist
    /// and will not create it if missing.
    public <T> T load(Path path, Class<T> configClass, YamlConfigurationProperties properties) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(properties, "properties");
        return YamlConfigurations.load(path, configClass, properties);
    }

    /// Creates a default properties builder with common settings.
    public YamlConfigurationProperties.Builder<?> defaultPropertiesBuilder() {
        return YamlConfigurationProperties.newBuilder()
                .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
                .setDeserializationCoercionTypes(DeserializationCoercionType.values());
    }

    /// Creates a default properties builder with environment variable resolution.
    ///
    /// @param envPrefix the environment variable prefix (e.g. `"MYPLUGIN_"`)
    /// @return a builder configured with env var resolution
    public YamlConfigurationProperties.Builder<?> defaultPropertiesBuilder(String envPrefix) {
        Objects.requireNonNull(envPrefix, "envPrefix");
        return defaultPropertiesBuilder()
                .setEnvVarResolutionConfiguration(
                        YamlConfigurationProperties.EnvVarResolutionConfiguration.resolveEnvVarsWithPrefix(
                                envPrefix, false));
    }

    /// Updates a configuration file with custom properties, only writing if changed.
    ///
    /// This method:
    /// 1. Loads the existing config (with Java defaults for any new fields)
    /// 2. Serializes it back to YAML (which includes new fields with defaults)
    /// 3. Compares with the original file content
    /// 4. Only writes if the content actually changed
    ///
    /// This ensures new fields are added to the file and obsolete fields are removed,
    /// but avoids unnecessary disk writes and timestamp updates when nothing changed.
    ///
    /// @param path the path to the configuration file
    /// @param configClass the configuration class
    /// @param properties custom ConfigLib properties
    /// @param <T> the configuration type
    /// @return the loaded configuration
    public <T> T updateIfChanged(Path path, Class<T> configClass, YamlConfigurationProperties properties) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(properties, "properties");

        // If file doesn't exist, just create it
        if (!Files.exists(path)) {
            return YamlConfigurations.update(path, configClass, properties);
        }

        try {
            // Read current file content as bytes
            byte[] oldContent = Files.readAllBytes(path);

            // Load existing config - fields not in file get Java defaults
            T config = YamlConfigurations.load(path, configClass, properties);

            // Serialize to bytes (this includes new fields with their defaults)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            YamlConfigurations.write(outputStream, configClass, config, properties);
            byte[] newContent = outputStream.toByteArray();

            // Only write if content changed
            if (!Arrays.equals(oldContent, newContent)) {
                AtomicFileWriter.write(path, newContent);
            }

            return config;
        } catch (IOException e) {
            throw new RuntimeException("Failed to update configuration: " + path, e);
        }
    }
}
