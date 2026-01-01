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
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.4")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.4.4")
    implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.3.0")
    implementation("io.freefair.gradle:lombok-plugin:8.14.2")
    implementation("org.openrewrite:plugin:7.23.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}
