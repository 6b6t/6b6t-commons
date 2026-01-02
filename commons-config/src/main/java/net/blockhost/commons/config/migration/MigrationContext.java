package net.blockhost.commons.config.migration;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/// Holds the state and data for a configuration migration.
///
/// The migration context provides access to the raw YAML data being migrated,
/// along with metadata about the migration process. It also provides utility
/// methods for common migration operations like getting nested values, renaming
/// fields, and type-safe value extraction.
///
/// ## Data Access
///
/// The [#data()] method returns a mutable map that migrations can modify directly:
///
/// ```java
/// @Override
/// public void migrate(MigrationContext context) {
///     Map<String, Object> data = context.data();
///     data.put("newField", "value");
///     data.remove("oldField");
/// }
/// ```
///
/// ## Nested Data Access
///
/// Use [#getNestedMap] and [#getNestedValue] to safely access nested structures:
///
/// ```java
/// @Override
/// public void migrate(MigrationContext context) {
///     // Safely get nested map, creating it if it doesn't exist
///     Map<String, Object> database = context.getOrCreateNestedMap("database");
///     database.put("poolSize", 10);
///
///     // Safely get a nested value with type casting
///     Optional<String> host = context.getNestedValue("database", "host", String.class);
/// }
/// ```
///
/// ## Immutability
///
/// The context itself is immutable (file path and current version cannot change),
/// but the data map is mutable to allow migrations to transform it.
///
/// @see Migration
/// @see MigrationExecutor
public final class MigrationContext {

    private final Path filePath;
    private final Map<String, Object> data;
    private final int currentVersion;
    private final int targetVersion;

    /// Creates a new migration context.
    ///
    /// @param filePath the path to the configuration file being migrated
    /// @param data the raw YAML data to migrate
    /// @param currentVersion the current version of the data
    /// @param targetVersion the target version to migrate to
    private MigrationContext(Path filePath, Map<String, Object> data, int currentVersion, int targetVersion) {
        this.filePath = filePath;
        this.data = data;
        this.currentVersion = currentVersion;
        this.targetVersion = targetVersion;
    }

    /// Creates a new migration context.
    ///
    /// @param filePath the path to the configuration file
    /// @param data the raw YAML data
    /// @param currentVersion the current version
    /// @param targetVersion the target version
    /// @return a new migration context
    /// @throws NullPointerException if filePath or data is null
    public static MigrationContext of(Path filePath, Map<String, Object> data, int currentVersion, int targetVersion) {
        Objects.requireNonNull(filePath, "filePath");
        Objects.requireNonNull(data, "data");
        return new MigrationContext(filePath, data, currentVersion, targetVersion);
    }

    /// Creates a migration context without a file path (for in-memory migrations).
    ///
    /// @param data the raw YAML data
    /// @param currentVersion the current version
    /// @param targetVersion the target version
    /// @return a new migration context
    /// @throws NullPointerException if data is null
    public static MigrationContext ofData(Map<String, Object> data, int currentVersion, int targetVersion) {
        Objects.requireNonNull(data, "data");
        return new MigrationContext(Path.of(""), data, currentVersion, targetVersion);
    }

    /// Returns the path to the configuration file being migrated.
    ///
    /// @return the file path, or an empty path for in-memory migrations
    public Path filePath() {
        return filePath;
    }

    /// Returns the mutable map containing the YAML data.
    ///
    /// Migrations should modify this map directly to transform the data.
    ///
    /// @return the data map
    public Map<String, Object> data() {
        return data;
    }

    /// Returns the version the data is currently at.
    ///
    /// This is the version before any migrations in the current batch are applied.
    ///
    /// @return the current version
    public int currentVersion() {
        return currentVersion;
    }

    /// Returns the target version to migrate to.
    ///
    /// @return the target version
    public int targetVersion() {
        return targetVersion;
    }

    /// Gets a value from the data map with type casting.
    ///
    /// @param key the key to look up
    /// @param type the expected type
    /// @param <T> the value type
    /// @return an Optional containing the value if present and of correct type
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    /// Gets a string value from the data map.
    ///
    /// @param key the key to look up
    /// @return an Optional containing the string value if present
    public Optional<String> getString(String key) {
        return get(key, String.class);
    }

    /// Gets an integer value from the data map.
    ///
    /// Handles both Integer and other Number types.
    ///
    /// @param key the key to look up
    /// @return an Optional containing the integer value if present
    public Optional<Integer> getInt(String key) {
        Object value = data.get(key);
        if (value instanceof Number number) {
            return Optional.of(number.intValue());
        }
        return Optional.empty();
    }

    /// Gets a boolean value from the data map.
    ///
    /// @param key the key to look up
    /// @return an Optional containing the boolean value if present
    public Optional<Boolean> getBoolean(String key) {
        return get(key, Boolean.class);
    }

    /// Gets a list value from the data map.
    ///
    /// @param key the key to look up
    /// @return an Optional containing the list if present
    @SuppressWarnings("unchecked")
    public Optional<List<Object>> getList(String key) {
        Object value = data.get(key);
        if (value instanceof List<?> list) {
            return Optional.of((List<Object>) list);
        }
        return Optional.empty();
    }

    /// Gets a nested map from the data.
    ///
    /// @param key the key for the nested map
    /// @return an Optional containing the nested map if present
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> getNestedMap(String key) {
        Object value = data.get(key);
        if (value instanceof Map<?, ?> map) {
            return Optional.of((Map<String, Object>) map);
        }
        return Optional.empty();
    }

    /// Gets or creates a nested map in the data.
    ///
    /// If the key doesn't exist, a new [LinkedHashMap] is created and added.
    ///
    /// @param key the key for the nested map
    /// @return the existing or newly created nested map
    /// @throws MigrationException if the key exists but is not a map
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOrCreateNestedMap(String key) {
        Object value = data.get(key);
        if (value == null) {
            Map<String, Object> newMap = new LinkedHashMap<>();
            data.put(key, newMap);
            return newMap;
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new MigrationException("Expected map at key '%s', but found: %s"
                .formatted(key, value.getClass().getName()));
    }

    /// Gets a value from a nested map.
    ///
    /// @param mapKey the key for the nested map
    /// @param valueKey the key within the nested map
    /// @param type the expected type
    /// @param <T> the value type
    /// @return an Optional containing the value if present
    public <T> Optional<T> getNestedValue(String mapKey, String valueKey, Class<T> type) {
        return getNestedMap(mapKey).flatMap(map -> {
            Object value = map.get(valueKey);
            if (value != null && type.isInstance(value)) {
                return Optional.of(type.cast(value));
            }
            return Optional.empty();
        });
    }

    /// Renames a key in the data map.
    ///
    /// If the old key doesn't exist, this method does nothing.
    /// If the new key already exists, it will be overwritten.
    ///
    /// @param oldKey the current key name
    /// @param newKey the new key name
    /// @return true if the key was renamed, false if the old key didn't exist
    public boolean rename(String oldKey, String newKey) {
        if (!data.containsKey(oldKey)) {
            return false;
        }
        data.put(newKey, data.remove(oldKey));
        return true;
    }

    /// Renames a key within a nested map.
    ///
    /// @param mapKey the key for the nested map
    /// @param oldKey the current key name within the nested map
    /// @param newKey the new key name within the nested map
    /// @return true if the key was renamed, false otherwise
    public boolean renameNested(String mapKey, String oldKey, String newKey) {
        return getNestedMap(mapKey)
                .map(map -> {
                    if (!map.containsKey(oldKey)) {
                        return false;
                    }
                    map.put(newKey, map.remove(oldKey));
                    return true;
                })
                .orElse(false);
    }

    /// Sets a value in the data map if the key is not already present.
    ///
    /// @param key the key
    /// @param value the default value
    /// @return true if the value was set, false if the key already existed
    public boolean setDefault(String key, @Nullable Object value) {
        if (data.containsKey(key)) {
            return false;
        }
        data.put(key, value);
        return true;
    }

    /// Moves a value from one location to a nested map.
    ///
    /// @param sourceKey the source key in the root map
    /// @param targetMapKey the key for the target nested map
    /// @param targetKey the key within the target nested map
    /// @return true if the value was moved, false if source didn't exist
    public boolean moveToNested(String sourceKey, String targetMapKey, String targetKey) {
        if (!data.containsKey(sourceKey)) {
            return false;
        }
        Object value = data.remove(sourceKey);
        getOrCreateNestedMap(targetMapKey).put(targetKey, value);
        return true;
    }

    /// Moves a value from a nested map to the root map.
    ///
    /// @param sourceMapKey the key for the source nested map
    /// @param sourceKey the key within the source nested map
    /// @param targetKey the target key in the root map
    /// @return true if the value was moved, false otherwise
    @SuppressWarnings("unchecked")
    public boolean moveFromNested(String sourceMapKey, String sourceKey, String targetKey) {
        Object mapValue = data.get(sourceMapKey);
        if (!(mapValue instanceof Map<?, ?> map)) {
            return false;
        }
        Map<String, Object> nestedMap = (Map<String, Object>) map;
        if (!nestedMap.containsKey(sourceKey)) {
            return false;
        }
        data.put(targetKey, nestedMap.remove(sourceKey));
        return true;
    }

    /// Creates a copy of the current data.
    ///
    /// Useful for creating backups before risky transformations.
    ///
    /// @return a deep copy of the data map
    @SuppressWarnings("unchecked")
    public Map<String, Object> copyData() {
        return deepCopy(data);
    }

    /// Performs a deep copy of a map structure.
    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> original) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> map) {
                copy.put(entry.getKey(), deepCopy((Map<String, Object>) map));
            } else if (value instanceof List<?> list) {
                copy.put(entry.getKey(), deepCopyList(list));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    /// Performs a deep copy of a list structure.
    @SuppressWarnings("unchecked")
    private List<Object> deepCopyList(List<?> original) {
        return original.stream()
                .map(item -> {
                    if (item instanceof Map<?, ?> map) {
                        return deepCopy((Map<String, Object>) map);
                    } else if (item instanceof List<?> list) {
                        return deepCopyList(list);
                    }
                    return item;
                })
                .toList();
    }

    @Override
    public String toString() {
        return "MigrationContext[filePath=%s, currentVersion=%d, targetVersion=%d]"
                .formatted(filePath, currentVersion, targetVersion);
    }
}
