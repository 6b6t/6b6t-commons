package net.blockhost.commons.config;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/// Utility class for loading and saving configuration files using ConfigLib.
///
/// This class provides a simplified API for working with ConfigLib's YAML configurations,
/// with sensible defaults and common configuration patterns.
///
/// Example usage:
/// <pre>
/// `// Define your configuration classclass MyConfig{private String host = "localhost";private int port = 3306;}// Load
// or create configurationPath configPath = dataFolder.resolve("config.yml");MyConfig config =
// ConfigLoader.loadOrCreate(configPath, MyConfig.class);`</pre>
///
/// @see de.exlll.configlib.Configuration
/// @see YamlConfigurations
public final class ConfigLoader {

    private ConfigLoader() {
        // Utility class
    }

    /// Loads a configuration from the specified path, or creates it with default values if it doesn't exist.
    ///
    /// This method uses the `update` strategy, which:
    ///
    ///     - Creates the file with defaults if it doesn't exist
    ///     - Adds any new fields that are in the class but not in the file
    ///     - Preserves existing values in the file
    ///     - Removes fields that are no longer in the class
    ///
    ///
    /// @param path            the path to the configuration file
    /// @param configClass     the configuration class
    /// @param <T>             the configuration type
    /// @return the loaded or created configuration
    public static <T> T loadOrCreate(Path path, Class<T> configClass) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        return YamlConfigurations.update(path, configClass);
    }

    /// Loads a configuration with custom properties.
    ///
    /// @param path            the path to the configuration file
    /// @param configClass     the configuration class
    /// @param properties      the configuration properties
    /// @param <T>             the configuration type
    /// @return the loaded or created configuration
    public static <T> T loadOrCreate(Path path, Class<T> configClass, YamlConfigurationProperties properties) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(properties, "properties");
        return YamlConfigurations.update(path, configClass, properties);
    }

    /// Loads a configuration with a customized properties builder.
    ///
    /// Example:
    /// <pre>
    /// `MyConfig config = ConfigLoader.loadOrCreate(configPath,MyConfig.class,builder -> builder.header("My Plugin
    // Configuration"));`</pre>
    ///
    /// @param path               the path to the configuration file
    /// @param configClass        the configuration class
    /// @param propertiesConsumer consumer to customize the properties builder
    /// @param <T>                the configuration type
    /// @return the loaded or created configuration
    public static <T> T loadOrCreate(
            Path path, Class<T> configClass, Consumer<YamlConfigurationProperties.Builder<?>> propertiesConsumer) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(propertiesConsumer, "propertiesConsumer");
        return YamlConfigurations.update(path, configClass, propertiesConsumer);
    }

    /// Saves a configuration to the specified path.
    ///
    /// This will overwrite any existing file at the path.
    ///
    /// @param path        the path to save to
    /// @param configClass the configuration class
    /// @param config      the configuration instance to save
    /// @param <T>         the configuration type
    public static <T> void save(Path path, Class<T> configClass, T config) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(config, "config");
        YamlConfigurations.save(path, configClass, config);
    }

    /// Saves a configuration with custom properties.
    ///
    /// @param path        the path to save to
    /// @param configClass the configuration class
    /// @param config      the configuration instance to save
    /// @param properties  the configuration properties
    /// @param <T>         the configuration type
    public static <T> void save(Path path, Class<T> configClass, T config, YamlConfigurationProperties properties) {
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
    ///
    /// @param path        the path to the configuration file
    /// @param configClass the configuration class
    /// @param <T>         the configuration type
    /// @return the loaded configuration
    /// @throws RuntimeException if the file doesn't exist or cannot be read
    public static <T> T load(Path path, Class<T> configClass) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(configClass, "configClass");
        return YamlConfigurations.load(path, configClass);
    }

    /// Creates a default properties builder with common settings.
    ///
    /// @return a new properties builder
    public static YamlConfigurationProperties.Builder<?> defaultPropertiesBuilder() {
        return YamlConfigurationProperties.newBuilder();
    }
}
