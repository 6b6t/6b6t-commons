plugins {
    `java-library`
    id("commons.formatting-conventions")
    id("io.freefair.lombok")
    id("net.ltgt.errorprone")
    id("com.github.spotbugs")
}

group = "net.blockhost.commons"

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.38.0")
    spotbugs("com.github.spotbugs:spotbugs:4.9.8")

    // Common annotations
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")

    // Testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.0")
    testImplementation("org.mockito:mockito-core:5.20.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks {
    compileJava {
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

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

spotbugs {
    ignoreFailures.set(false)
    showStackTraces.set(true)
    showProgress.set(true)
}
