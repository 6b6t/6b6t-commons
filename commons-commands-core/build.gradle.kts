plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "StrokkCommands core module for 6b6t plugins - platform-independent utilities"

dependencies {
    api(project(":commons-config"))
    api(project(":commons-message"))
    api(libs.brigadier)
    api(libs.strokkcommands.annotations.common)
    api(libs.strokkcommands.annotations.common.permission)
    api(libs.strokkcommands.processor.common)
}
