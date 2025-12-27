plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Message utilities for 6b6t plugins - MiniMessage formatting"

dependencies {
    api(libs.adventure.api)
    api(libs.adventure.text.minimessage)

    compileOnly(libs.paper.api)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
