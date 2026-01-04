/// Redis database utilities for 6b6t plugins.
///
/// This package provides Redis connectivity using Jedis with connection pooling
/// support and ConfigLib integration for configuration.
///
/// ## Quick Start
///
/// ```java
/// // Using ConfigLib configuration
/// @Configuration
/// public class PluginConfig {
///     private RedisConfig redis = new RedisConfig();
///     public RedisConfig redis() { return redis; }
/// }
///
/// // Create RedisManager from config
/// RedisManager redisManager = RedisManager.builder()
///     .config(config.redis())
///     .logger(plugin.getLogger())
///     .build();
///
/// redisManager.connect();
///
/// // Use connections with callback pattern
/// redisManager.withConnection(jedis -> {
///     jedis.set("key", "value");
/// });
///
/// // Get values
/// String value = redisManager.withConnectionResult(jedis -> jedis.get("key"));
///
/// // Shutdown
/// redisManager.shutdown();
/// ```
///
/// @see net.blockhost.commons.database.redis.RedisConfig
/// @see net.blockhost.commons.database.redis.RedisManager
@NullMarked
package net.blockhost.commons.database.redis;

import org.jspecify.annotations.NullMarked;
