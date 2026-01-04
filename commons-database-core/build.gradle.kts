plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Core database utilities for 6b6t plugins - HikariCP connection pooling"

dependencies {
    api(projects.commonsConfig)
    api(libs.hikaricp)

    // Immutables for staged builders
    compileOnly(libs.immutables.annotations)
    annotationProcessor(libs.immutables.value)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
