plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Command framework core for 6b6t plugins - platform-independent abstractions"

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
