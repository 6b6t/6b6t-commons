package net.blockhost.commons.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationHolderTest {
    @Test
    void loadsOnceDuringConstructionAndAgainOnReload() {
        AtomicInteger loads = new AtomicInteger();
        ConfigurationHolder<Integer> holder = new ConfigurationHolder<>(loads::incrementAndGet) {};

        assertEquals(1, holder.get());
        assertEquals(1, loads.get());
        assertEquals(2, holder.reload());
        assertEquals(2, holder.get());
    }

    @Test
    void preservesSaveMetadata() {
        Path path = Path.of("config.yml");
        ConfigurationHolder<String> holder = new ConfigurationHolder<>(() -> "initial", path, String.class) {};

        assertEquals("initial", holder.get());
        assertEquals(path, holder.getConfigPath());
    }

    @Test
    void propagatesInitialLoadFailureForBothConstructors() {
        IllegalStateException failure = new IllegalStateException("invalid configuration");
        assertSame(
                failure,
                assertThrows(
                        IllegalStateException.class,
                        () -> new ConfigurationHolder<>(() -> {
                            throw failure;
                        }) {}));
        assertSame(
                failure,
                assertThrows(
                        IllegalStateException.class,
                        () -> new ConfigurationHolder<>(
                                () -> {
                                    throw failure;
                                },
                                Path.of("config.yml"),
                                String.class) {}));
    }
}
