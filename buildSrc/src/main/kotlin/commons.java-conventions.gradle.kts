plugins {
    `java-library`
    id("commons.formatting-conventions")
    id("io.freefair.lombok")
    id("net.ltgt.errorprone")
    id("com.github.spotbugs")
}

group = "net.blockhost.commons"

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.36.0")
    spotbugs("com.github.spotbugs:spotbugs:4.8.6")

    // Common annotations
    compileOnly("org.jetbrains:annotations:26.0.1")
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.8.6")

    // Testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
                links("https://docs.oracle.com/en/java/javase/21/docs/api/")
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
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
    excludeFilter.set(rootProject.file("config/spotbugs/exclude.xml"))
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") {
        required.set(true)
        outputLocation.set(layout.buildDirectory.file("reports/spotbugs/${name}.html"))
    }
    reports.create("xml") {
        required.set(false)
    }
}
