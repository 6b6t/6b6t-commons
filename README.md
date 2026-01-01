# 6b6t-commons

Common utilities and shared code for 6b6t Minecraft plugins.

## Modules

| Module | Description |
|--------|-------------|
| `commons-core` | Core utilities (time formatting, etc.) |
| `commons-config` | ConfigLib YAML configuration utilities |
| `commons-database` | MariaDB/HikariCP database utilities |
| `commons-message` | MiniMessage formatting and messaging |
| `commons-commands-core` | StrokkCommands core with Brigadier |
| `commons-commands-bukkit` | StrokkCommands for Bukkit/Paper |
| `commons-commands-velocity` | StrokkCommands for Velocity |

## Installation

### Prerequisites

- Java 25 or higher
- Gradle build tool

### Adding the Repository

Add the repositories to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven("https://6b6t.github.io/6b6t-commons/maven")
    // Required for StrokkCommands
    maven("https://eldonexus.de/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-snapshots/")
}
```

### Adding Dependencies

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
    compileOnly("net.blockhost.commons:commons-commands-bukkit:1.0.0-SNAPSHOT")
    annotationProcessor("net.strokkur.commands:processor-paper:2.0.0-SNAPSHOT")
    // For Velocity plugins:
    compileOnly("net.blockhost.commons:commons-commands-velocity:1.0.0-SNAPSHOT")
    annotationProcessor("net.strokkur.commands:processor-velocity:2.0.0-SNAPSHOT")
}
```

## Documentation

### Javadoc

API documentation is automatically generated and published to GitHub Pages:

**https://6b6t.github.io/6b6t-commons/javadoc/**

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

#### Command Framework (StrokkCommands)

The command modules use [StrokkCommands](https://commands.strokkur.net/docs/) v2.0.0, an annotation-based 
command framework that generates Brigadier commands at compile time with zero runtime overhead.

**Documentation:** https://commands.strokkur.net/docs/

```java
// Define a command using annotations
@Command("example")
@Aliases("ex")
@Description("An example command")
public class ExampleCommand {

    @Executes
    void execute(CommandSender sender) {
        sender.sendRichMessage("<green>Hello from StrokkCommands!");
    }

    @Executes("greet")
    void greet(CommandSender sender, Player target) {
        sender.sendRichMessage("<yellow>Hello, " + target.getName() + "!");
    }
    
    @Executes("teleport")
    void teleport(CommandSender sender, @Executor Player player, Player target) {
        player.teleport(target);
        sender.sendRichMessage("<green>Teleported to " + target.getName());
    }
}

// Register the generated command in your plugin's onLoad()
public void onLoad() {
    getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
        ExampleCommandBrigadier.register(event.registrar());
    }));
}
```

**Key Features:**
- Annotation-based command definition
- Compile-time code generation (zero runtime overhead)
- Native Brigadier integration with full argument type support
- Automatic tab completion
- Permission support via `@Permission` annotation
- Subcommand support

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
- **Publish**: Artifacts and Javadoc are published to GitHub Pages on pushes to `main`/`master` or version tags (`v*`)
- **Maven Repository**: https://6b6t.github.io/6b6t-commons/maven/
- **Javadoc**: https://6b6t.github.io/6b6t-commons/javadoc/

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
