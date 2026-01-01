plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "StrokkCommands core module for 6b6t plugins - platform-independent utilities"

dependencies {
    api(project(":commons-config"))
    api(libs.brigadier)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
