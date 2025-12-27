plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Configuration utilities for 6b6t plugins - ConfigLib integration"

dependencies {
    api(libs.configlib.yaml)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
