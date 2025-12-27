package net.blockhost.commons.config;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/// Utility class for loading and saving configuration files using ConfigLib.
///
/// This class provides a simplified API for working with ConfigLib's YAML configurations,
/// with sensible defaults and common configuration patterns.
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
/// MyConfig config = ConfigLoader.loadOrCreate(configPath, MyConfig.class);
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
    public <T> T loadOrCreate(Path path, Class<T> configClass) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        return YamlConfigurations.update(path, configClass);
    }

    /// Loads a configuration with custom properties.
    public <T> T loadOrCreate(Path path, Class<T> configClass, YamlConfigurationProperties properties) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(properties, "properties");
        return YamlConfigurations.update(path, configClass, properties);
    }

    /// Loads a configuration with a customized properties builder.
    ///
    /// Example:
    /// ```java
    /// MyConfig config = ConfigLoader.loadOrCreate(
    ///     configPath,
    ///     MyConfig.class,
    ///     builder -> builder.header("My Plugin Configuration")
    /// );
    /// ```
    public <T> T loadOrCreate(
            Path path, Class<T> configClass, Consumer<YamlConfigurationProperties.Builder<?>> propertiesConsumer) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(propertiesConsumer, "propertiesConsumer");
        return YamlConfigurations.update(path, configClass, propertiesConsumer);
    }

    /// Saves a configuration to the specified path.
    ///
    /// This will overwrite any existing file at the path.
    public <T> void save(Path path, Class<T> configClass, T config) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(config, "config");
        YamlConfigurations.save(path, configClass, config);
    }

    /// Saves a configuration with custom properties.
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
    public <T> T load(Path path, Class<T> configClass) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        return YamlConfigurations.load(path, configClass);
    }

    /// Creates a default properties builder with common settings.
    public YamlConfigurationProperties.Builder<?> defaultPropertiesBuilder() {
        return YamlConfigurationProperties.newBuilder();
    }
}
