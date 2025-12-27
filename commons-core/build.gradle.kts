plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Core utilities for 6b6t plugins"

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
