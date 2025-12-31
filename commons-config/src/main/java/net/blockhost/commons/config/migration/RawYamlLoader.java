package net.blockhost.commons.config.migration;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/// Utility class for loading and saving raw YAML files as `Map` structures.
///
/// This class provides low-level YAML operations using SnakeYAML, allowing direct
/// manipulation of YAML data without mapping to Java objects. This is essential
/// for the migration system, which needs to transform YAML structures before
/// they can be loaded into typed configuration classes.
///
/// ## Features
///
/// - **Safe loading**: Uses [SafeConstructor] to prevent arbitrary code execution
/// - **Order preservation**: Uses [LinkedHashMap] to maintain key insertion order
/// - **Pretty output**: Configures block style formatting with proper indentation
/// - **Flexible input**: Supports loading from [Path], [InputStream], [Reader], or [String]
///
/// ## Usage
///
/// ```java
/// // Load YAML from file
/// Path configPath = Path.of("config.yml");
/// Map<String, Object> data = RawYamlLoader.load(configPath);
///
/// // Modify the data
/// data.put("version", 2);
/// data.put("newField", "newValue");
///
/// // Save back to file
/// RawYamlLoader.save(configPath, data);
/// ```
///
/// ## Thread Safety
///
/// Each method creates a new [Yaml] instance, making this class thread-safe.
/// However, the returned `Map` objects are **not** thread-safe and should be
/// synchronized externally if accessed from multiple threads.
///
/// @see Migration
/// @see MigrationContext
@UtilityClass
public class RawYamlLoader {

    private static final int DEFAULT_INDENT = 2;
    private static final int DEFAULT_INDICATOR_INDENT = 0;

    /// Loads YAML content from a file path.
    ///
    /// If the file does not exist, returns an empty [LinkedHashMap].
    ///
    /// @param path the path to the YAML file
    /// @return a mutable map containing the YAML data, or empty map if file doesn't exist
    /// @throws MigrationException if the file cannot be read or parsed
    /// @throws NullPointerException if path is null
    public Map<String, Object> load(Path path) {
        Objects.requireNonNull(path, "path");

        if (!Files.exists(path)) {
            return new LinkedHashMap<>();
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            return load(inputStream);
        } catch (IOException e) {
            throw new MigrationException("Failed to read YAML file: " + path, e);
        }
    }

    /// Loads YAML content from an input stream.
    ///
    /// The caller is responsible for closing the input stream.
    ///
    /// @param inputStream the input stream containing YAML content
    /// @return a mutable map containing the YAML data
    /// @throws MigrationException if the content cannot be parsed
    /// @throws NullPointerException if inputStream is null
    public Map<String, Object> load(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream");

        try {
            Yaml yaml = createYaml();
            Map<String, Object> result = yaml.load(inputStream);
            return result != null ? new LinkedHashMap<>(result) : new LinkedHashMap<>();
        } catch (Exception e) {
            throw new MigrationException("Failed to parse YAML content", e);
        }
    }

    /// Loads YAML content from a reader.
    ///
    /// The caller is responsible for closing the reader.
    ///
    /// @param reader the reader containing YAML content
    /// @return a mutable map containing the YAML data
    /// @throws MigrationException if the content cannot be parsed
    /// @throws NullPointerException if reader is null
    public Map<String, Object> load(Reader reader) {
        Objects.requireNonNull(reader, "reader");

        try {
            Yaml yaml = createYaml();
            Map<String, Object> result = yaml.load(reader);
            return result != null ? new LinkedHashMap<>(result) : new LinkedHashMap<>();
        } catch (Exception e) {
            throw new MigrationException("Failed to parse YAML content", e);
        }
    }

    /// Loads YAML content from a string.
    ///
    /// @param content the YAML content as a string
    /// @return a mutable map containing the YAML data
    /// @throws MigrationException if the content cannot be parsed
    /// @throws NullPointerException if content is null
    public Map<String, Object> loadFromString(String content) {
        Objects.requireNonNull(content, "content");

        if (content.isBlank()) {
            return new LinkedHashMap<>();
        }

        try {
            Yaml yaml = createYaml();
            Map<String, Object> result = yaml.load(content);
            return result != null ? new LinkedHashMap<>(result) : new LinkedHashMap<>();
        } catch (Exception e) {
            throw new MigrationException("Failed to parse YAML content", e);
        }
    }

    /// Saves YAML data to a file path.
    ///
    /// Creates parent directories if they don't exist. Overwrites any existing file.
    ///
    /// @param path the path to save the YAML file
    /// @param data the data to save
    /// @throws MigrationException if the file cannot be written
    /// @throws NullPointerException if path or data is null
    public void save(Path path, Map<String, Object> data) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(data, "data");

        try {
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            try (OutputStream outputStream = Files.newOutputStream(path)) {
                save(outputStream, data);
            }
        } catch (IOException e) {
            throw new MigrationException("Failed to write YAML file: " + path, e);
        }
    }

    /// Saves YAML data to an output stream.
    ///
    /// The caller is responsible for closing the output stream.
    ///
    /// @param outputStream the output stream to write to
    /// @param data the data to save
    /// @throws MigrationException if the data cannot be serialized
    /// @throws NullPointerException if outputStream or data is null
    public void save(OutputStream outputStream, Map<String, Object> data) {
        Objects.requireNonNull(outputStream, "outputStream");
        Objects.requireNonNull(data, "data");

        try {
            Yaml yaml = createYaml();
            yaml.dump(data, new java.io.OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new MigrationException("Failed to serialize YAML content", e);
        }
    }

    /// Saves YAML data to a writer.
    ///
    /// The caller is responsible for closing the writer.
    ///
    /// @param writer the writer to write to
    /// @param data the data to save
    /// @throws MigrationException if the data cannot be serialized
    /// @throws NullPointerException if writer or data is null
    public void save(Writer writer, Map<String, Object> data) {
        Objects.requireNonNull(writer, "writer");
        Objects.requireNonNull(data, "data");

        try {
            Yaml yaml = createYaml();
            yaml.dump(data, writer);
        } catch (Exception e) {
            throw new MigrationException("Failed to serialize YAML content", e);
        }
    }

    /// Converts YAML data to a string.
    ///
    /// @param data the data to convert
    /// @return the YAML content as a string
    /// @throws MigrationException if the data cannot be serialized
    /// @throws NullPointerException if data is null
    public String saveToString(Map<String, Object> data) {
        Objects.requireNonNull(data, "data");

        try {
            Yaml yaml = createYaml();
            return yaml.dump(data);
        } catch (Exception e) {
            throw new MigrationException("Failed to serialize YAML content", e);
        }
    }

    /// Reads the version field from a YAML file.
    ///
    /// This is a convenience method for quickly checking the version of a configuration
    /// file without loading the entire content.
    ///
    /// @param path the path to the YAML file
    /// @return the version number, or 0 if the file doesn't exist or has no version field
    /// @throws MigrationException if the file cannot be read or the version is not a valid integer
    /// @throws NullPointerException if path is null
    public int readVersion(Path path) {
        Objects.requireNonNull(path, "path");

        Map<String, Object> data = load(path);
        return extractVersion(data);
    }

    /// Extracts the version field from YAML data.
    ///
    /// @param data the YAML data map
    /// @return the version number, or 0 if no version field exists
    /// @throws MigrationException if the version field is not a valid integer
    /// @throws NullPointerException if data is null
    public int extractVersion(Map<String, Object> data) {
        Objects.requireNonNull(data, "data");

        Object versionObj = data.get("version");
        if (versionObj == null) {
            return 0;
        }

        if (versionObj instanceof Number number) {
            return number.intValue();
        }

        if (versionObj instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                throw new MigrationException("Invalid version format: " + str, e);
            }
        }

        throw new MigrationException(
                "Version field must be a number, got: " + versionObj.getClass().getName());
    }

    /// Creates a new Yaml instance with default configuration.
    ///
    /// The configuration includes:
    /// - Safe constructor (prevents arbitrary code execution)
    /// - Block flow style (human-readable format)
    /// - 2-space indentation
    /// - UTF-8 encoding
    private Yaml createYaml() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(DEFAULT_INDENT);
        dumperOptions.setIndicatorIndent(DEFAULT_INDICATOR_INDENT);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        Representer representer = new Representer(dumperOptions);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        return new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions, loaderOptions);
    }
}
