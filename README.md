# 6b6t-commons

Common utilities and shared code for 6b6t Minecraft plugins.

## Modules

| Module | Description | Javadoc |
|--------|-------------|---------|
| `commons-core` | Core utilities (time formatting, etc.) | [API Docs](https://6b6t.github.io/6b6t-commons/javadoc/commons-core/) |
| `commons-config` | ConfigLib YAML configuration utilities | [API Docs](https://6b6t.github.io/6b6t-commons/javadoc/commons-config/) |
| `commons-message` | MiniMessage formatting and messaging | [API Docs](https://6b6t.github.io/6b6t-commons/javadoc/commons-message/) |
| `commons-commands-core` | StrokkCommands core with Brigadier | [API Docs](https://6b6t.github.io/6b6t-commons/javadoc/commons-commands-core/) |

### Database Modules

| Module | Description | Javadoc |
|--------|-------------|---------|
| `commons-database-core` | Dialect-independent HikariCP connection pooling | [API Docs](https://6b6t.github.io/6b6t-commons/javadoc/commons-database-core/) |
| `commons-database-mariadb` | MariaDB/MySQL configuration and support | [API Docs](https://6b6t.github.io/6b6t-commons/javadoc/commons-database-mariadb/) |
| `commons-database-postgres` | PostgreSQL configuration and support | [API Docs](https://6b6t.github.io/6b6t-commons/javadoc/commons-database-postgres/) |
| `commons-database-redis` | Redis connectivity with Jedis | [API Docs](https://6b6t.github.io/6b6t-commons/javadoc/commons-database-redis/) |

## Installation

### Prerequisites

- Java 25 or higher
- Gradle build tool

### Maven Repository

Add the repository to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven("https://6b6t.github.io/6b6t-commons/maven")
    // Required for StrokkCommands
    maven("https://eldonexus.de/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-snapshots/")
}
```

### Artifacts

All artifacts use group ID `net.blockhost` and version `1.1.0-SNAPSHOT`.

```kotlin
dependencies {
    // Core utilities
    implementation("net.blockhost:commons-core:1.1.0-SNAPSHOT")

    // Configuration (ConfigLib)
    implementation("net.blockhost:commons-config:1.1.0-SNAPSHOT")

    // Messaging (MiniMessage)
    implementation("net.blockhost:commons-message:1.1.0-SNAPSHOT")

    // Database - MariaDB (includes core)
    implementation("net.blockhost:commons-database-mariadb:1.1.0-SNAPSHOT")

    // Database - PostgreSQL (includes core)
    implementation("net.blockhost:commons-database-postgres:1.1.0-SNAPSHOT")

    // Database - Redis
    implementation("net.blockhost:commons-database-redis:1.1.0-SNAPSHOT")
}
```

## Documentation

API documentation with usage examples is available in the Javadoc:

- **Aggregated Javadoc**: https://6b6t.github.io/6b6t-commons/javadoc/
- **StrokkCommands Documentation**: https://commands.strokkur.net/docs/

## Building from Source

```bash
git clone https://github.com/6b6t/6b6t-commons.git
cd 6b6t-commons
./gradlew build
./gradlew aggregateJavadoc
```

## CI/CD

- **Maven Repository**: https://6b6t.github.io/6b6t-commons/maven/
- **Javadoc**: https://6b6t.github.io/6b6t-commons/javadoc/

## License

MIT License - see [LICENSE](LICENSE) for details.

## Local Git checks

Install [Lefthook](https://lefthook.dev/installation/) 1.13.6 or newer and Python 3.10 or newer.
Run this command once in each clone or linked worktree:

```bash
lefthook install
```

The commit hook checks staged whitespace without changing files or staging content.
Before a push, the hook reads Git's exact ref updates and selects checks from the changed paths, including deleted files.
New refs and unavailable remote baselines check the complete tree.
Check out the commit that you push. Commit or stash tracked and untracked changes first so that checks use the pushed code.
Deletion-only pushes do not run code checks. Hook regression tests run for every push with changed files.
Documentation-only changes do not compile the application. CI remains enabled.

Use the Java toolchain selected by Gradle.
Java, Gradle, and analysis configuration changes run `./gradlew check spotlessCheck --max-workers=6 --no-configuration-cache`.
These checks compile code, run tests, and run static analysis without publishing artifacts.

Preview all local checks and their selected paths:

```bash
python3 scripts/check-push.py --all --dry-run --verbose
```

Run all checks against the current working tree before committing:

```bash
python3 scripts/check-push.py --all
```

The `--all` command checks working files directly. It does not require a clean tree.
Normal Git pushes require a clean tree and reject a ref that points to another commit.
