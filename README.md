# 6b6t-commons

Common utilities and shared code for 6b6t Minecraft plugins.

## Modules

| Module | Description |
|--------|-------------|
| `commons-core` | Core utilities (time formatting, etc.) |
| `commons-config` | ConfigLib YAML configuration utilities |
| `commons-database` | MariaDB/HikariCP database utilities |
| `commons-message` | MiniMessage formatting and messaging |
| `commons-command-core` | Platform-independent command framework |
| `commons-command-bukkit` | Bukkit/Paper command implementation |
| `commons-command-velocity` | Velocity proxy command implementation |

## Installation

### Prerequisites

- Java 25 or higher
- Gradle or Maven build tool
- GitHub account (for accessing GitHub Packages)

### GitHub Packages Authentication

Since artifacts are published to GitHub Packages, you need to authenticate to download them.

#### Gradle (Kotlin DSL)

Add the repository to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/6b6t/6b6t-commons")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

Add credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

> **Note:** Generate a personal access token at https://github.com/settings/tokens with `read:packages` scope.

#### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/6b6t/6b6t-commons")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

#### Maven

Add the repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/6b6t/6b6t-commons</url>
    </repository>
</repositories>
```

Configure authentication in `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

### Adding Dependencies

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
    // Core utilities
    implementation("net.blockhost.commons:commons-core:1.0.0-SNAPSHOT")
    
    // Configuration (ConfigLib)
    implementation("net.blockhost.commons:commons-config:1.0.0-SNAPSHOT")
    
    // Database (MariaDB + HikariCP)
    implementation("net.blockhost.commons:commons-database:1.0.0-SNAPSHOT")
    
    // Messaging (MiniMessage)
    implementation("net.blockhost.commons:commons-message:1.0.0-SNAPSHOT")
    
    // Commands - choose based on your platform:
    // For Bukkit/Paper plugins:
    implementation("net.blockhost.commons:commons-command-bukkit:1.0.0-SNAPSHOT")
    // For Velocity plugins:
    implementation("net.blockhost.commons:commons-command-velocity:1.0.0-SNAPSHOT")
}
```

#### Maven

```xml
<dependencies>
    <!-- Core utilities -->
    <dependency>
        <groupId>net.blockhost.commons</groupId>
        <artifactId>commons-core</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- Configuration (ConfigLib) -->
    <dependency>
        <groupId>net.blockhost.commons</groupId>
        <artifactId>commons-config</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- Database (MariaDB + HikariCP) -->
    <dependency>
        <groupId>net.blockhost.commons</groupId>
        <artifactId>commons-database</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- Messaging (MiniMessage) -->
    <dependency>
        <groupId>net.blockhost.commons</groupId>
        <artifactId>commons-message</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- Commands - Bukkit/Paper -->
    <dependency>
        <groupId>net.blockhost.commons</groupId>
        <artifactId>commons-command-bukkit</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- Commands - Velocity -->
    <dependency>
        <groupId>net.blockhost.commons</groupId>
        <artifactId>commons-command-velocity</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Documentation

### Javadoc

API documentation is automatically generated and published to GitHub Pages:

**https://6b6t.github.io/6b6t-commons/**

### Quick Examples

#### Database Configuration

```java
// Using ConfigLib for configuration
@Configuration
public class PluginConfig {
    @Comment("Database settings")
    private DatabaseConfig database = new DatabaseConfig();
    
    public DatabaseConfig database() {
        return database;
    }
}

// Loading config and creating connection pool
PluginConfig config = ConfigLoader.loadOrCreate(configPath, PluginConfig.class);
HikariDataSource dataSource = HikariDataSourceBuilder.createDataSource(config.database());

// Using the connection
try (Connection conn = dataSource.getConnection()) {
    // Execute queries
}
```

#### Command Framework (Bukkit)

```java
// Create a subcommand
public class HelpSubCommand implements BukkitSubCommand {
    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        player.sendMessage("Help message here");
    }
    
    @Override
    public @NotNull String getName() {
        return "help";
    }
}

// Register commands
BukkitCommandDispatcher dispatcher = BukkitCommandDispatcher.builder()
    .defaultSubCommand("help")
    .register(new HelpSubCommand())
    .unknownSubCommandHandler((player, args) -> 
        player.sendMessage("Unknown command: " + args[0]))
    .build();

plugin.getCommand("mycommand").setExecutor(dispatcher);
plugin.getCommand("mycommand").setTabCompleter(dispatcher);
```

#### Message Service

```java
// Create message service with config provider
MessageService messages = MessageService.create(key -> config.getMessage(key));

// Send messages
messages.send(player, "welcome");
messages.send(player, "balance", Map.of("amount", "1000"));

// Parse MiniMessage directly
Component component = MessageService.parse("<green>Hello <bold>World</bold>!");
```

#### Time Formatting

```java
// Format durations
TimeFormatter.formatSeconds(3661);  // "1h 1m 1s"
TimeFormatter.formatCompact(125);   // "2:05"
TimeFormatter.formatLong(3661);     // "1 hour, 1 minute, 1 second"

// Parse time strings
Duration duration = TimeFormatter.parse("1h30m");  // 90 minutes
long seconds = TimeFormatter.parseToSeconds("2d"); // 172800
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/6b6t/6b6t-commons.git
cd 6b6t-commons

# Build all modules
./gradlew build

# Generate Javadoc
./gradlew aggregateJavadoc

# Run tests
./gradlew test
```

## CI/CD

This project uses GitHub Actions for continuous integration:

- **Build & Test**: Runs on every push and pull request
- **Publish**: Artifacts are published to GitHub Packages on pushes to `main`/`master` or version tags (`v*`)
- **Javadoc**: Documentation is deployed to GitHub Pages automatically

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
