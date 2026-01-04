plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Message utilities for 6b6t plugins - MiniMessage formatting"

dependencies {
    api(libs.adventure.api)
    api(libs.adventure.text.minimessage)
    api(libs.configlib.yaml)

    compileOnly(libs.paper.api)
}
