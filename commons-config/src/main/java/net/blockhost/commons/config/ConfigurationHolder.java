package net.blockhost.commons.config;

import net.blockhost.commons.config.migration.ConfigMigrator;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/// A holder for configuration instances that supports dependency injection and reloading.
///
/// This class wraps a configuration instance and provides a way to reload it on demand
/// while maintaining a consistent reference for consumers. This is particularly useful
/// when the configuration needs to be injected into other services that should always
/// use the latest configuration without holding stale references.
///
/// ## Extending for Plugin-Specific Holders
///
/// Create a plugin-specific holder by extending this class:
/// ```java
/// public class MyPluginConfigHolder extends ConfigurationHolder<MyPluginConfig> {
///     public MyPluginConfigHolder(Path configPath, ConfigMigrator migrator) {
///         super(() -> migrator.migrateAndLoad(configPath, MyPluginConfig.class, MyPluginConfig.CURRENT_VERSION));
///     }
/// }
/// ```
///
/// Then inject the specific holder type:
/// ```java
/// public class MyService {
///     private final MyPluginConfigHolder config;
///
///     public MyService(MyPluginConfigHolder config) {
///         this.config = config;
///     }
///
///     public void doSomething() {
///         MyPluginConfig cfg = config.get();  // No type parameter needed
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// This class is thread-safe. The configuration reference is stored in an [AtomicReference],
/// ensuring visibility across threads after reload.
///
/// @param <T> the configuration type
/// @see ConfigLoader
/// @see ConfigMigrator
public abstract class ConfigurationHolder<T> {

    private final AtomicReference<T> configRef;
    private final Supplier<T> loader;
    private @Nullable Consumer<T> reloadCallback;
    private @Nullable Path configPath;
    private @Nullable Class<T> configClass;

    /// Protected constructor for subclasses.
    ///
    /// @param loader the function that loads the configuration
    protected ConfigurationHolder(Supplier<T> loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.configRef = new AtomicReference<>(loader.get());
    }

    /// Protected constructor for subclasses with save support.
    ///
    /// @param loader the function that loads the configuration
    /// @param configPath the path to save the configuration to
    /// @param configClass the configuration class for saving
    protected ConfigurationHolder(Supplier<T> loader, Path configPath, Class<T> configClass) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.configPath = configPath;
        this.configClass = configClass;
        this.configRef = new AtomicReference<>(loader.get());
    }

    /// Gets the current configuration instance.
    ///
    /// This method always returns the most recent configuration,
    /// even after a [#reload()] call from another thread.
    ///
    /// @return the current configuration
    public T get() {
        return configRef.get();
    }

    /// Reloads the configuration from disk.
    ///
    /// This method reloads the configuration using the loader provided during construction.
    /// After this call, subsequent calls to [#get()] will return the new configuration.
    ///
    /// This operation is thread-safe.
    ///
    /// @return the newly loaded configuration
    public T reload() {
        T newConfig = loader.get();
        configRef.set(newConfig);
        if (reloadCallback != null) {
            reloadCallback.accept(newConfig);
        }
        return newConfig;
    }

    /// Sets a callback to be invoked after each reload.
    ///
    /// This is useful for performing post-reload actions like reinitializing
    /// services that depend on the configuration.
    ///
    /// @param callback the callback to invoke after reload
    /// @return this holder for chaining
    public ConfigurationHolder<T> onReload(Consumer<T> callback) {
        this.reloadCallback = callback;
        return this;
    }

    /// Saves the current configuration to disk.
    /// Requires the holder to be created with path and class information.
    ///
    /// @throws IllegalStateException if path or configClass was not set during construction
    public void save() {
        if (configPath == null || configClass == null) {
            throw new IllegalStateException(
                    "Cannot save: path or configClass not set. Use save(path, configClass) instead.");
        }
        ConfigLoader.save(configPath, configClass, configRef.get());
    }

    /// Saves the current configuration to disk.
    ///
    /// @param path the path to save to
    /// @param configClass the configuration class
    public void save(Path path, Class<T> configClass) {
        ConfigLoader.save(path, configClass, configRef.get());
    }

    /// Gets the path where this configuration is stored.
    ///
    /// @return the configuration path, or null if not set
    public @Nullable Path getConfigPath() {
        return configPath;
    }
}
