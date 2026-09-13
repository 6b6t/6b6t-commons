plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "PaperMC Repository"
    }
}

dependencies {
    // Version catalog access - see https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // Gradle plugins
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.2.1")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.5.11")
    implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.4.0")
    implementation("io.freefair.gradle:lombok-plugin:8.14.4")
    implementation("org.openrewrite:plugin:7.41.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
}
