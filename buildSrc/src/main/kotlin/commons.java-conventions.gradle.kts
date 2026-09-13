import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    id("commons.formatting-conventions")
    id("io.freefair.lombok")
    id("net.ltgt.errorprone")
    id("com.github.spotbugs")
    id("org.openrewrite.rewrite")
}

rewrite {
    activeRecipe("org.openrewrite.java.format.AutoFormat")
    activeRecipe("org.openrewrite.staticanalysis.CommonStaticAnalysis")
    activeRecipe("org.openrewrite.staticanalysis.CodeCleanup")
    activeRecipe("org.openrewrite.staticanalysis.JavaApiBestPractices")
    activeRecipe("org.openrewrite.java.testing.junit5.JUnit5BestPractices")
    activeRecipe("org.openrewrite.java.testing.cleanup.BestPractices")
    activeRecipe("org.openrewrite.java.migrate.UpgradeToJava25")
    isExportDatatables = true
}

dependencies {
    errorprone("com.uber.nullaway:nullaway:0.14.1")
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    spotbugs("com.github.spotbugs:spotbugs:4.10.4")

    // OpenRewrite recipes
    rewrite("org.openrewrite.recipe:rewrite-static-analysis:2.41.1")
    rewrite("org.openrewrite.recipe:rewrite-migrate-java:3.42.1")
    rewrite("org.openrewrite.recipe:rewrite-testing-frameworks:3.44.0")

    // Immutables
    annotationProcessor("org.immutables:value:2.12.2")
    compileOnly("org.immutables:value-annotations:2.12.2")

    // Common annotations
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.10.4")
    api("org.jspecify:jspecify:1.0.1")

    // Testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.4")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.errorprone {
            disableWarningsInGeneratedCode = true
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "com.uber")
        }
        // Include to disable NullAway on test code
        if (name.lowercase().contains("test")) {
            options.errorprone {
                disable("NullAway")
            }
        }
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(
            listOf(
                "-parameters",
                "-Xlint:all",
                "-Xlint:-processing",
                "-Xlint:-serial"
            )
        )
        options.isFork = true
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    javadoc {
        isFailOnError = false
        options {
            (this as StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                charSet = "UTF-8"
                links("https://docs.oracle.com/en/java/javase/25/docs/api/")
                // Suppress warnings for missing javadoc during development
                addStringOption("Xdoclint:none", "-quiet")
            }
        }
    }
}

spotbugs {
    ignoreFailures.set(false)
    showStackTraces.set(true)
    showProgress.set(true)
    excludeFilter.set(rootProject.file("config/spotbugs/exclude.xml"))
}
