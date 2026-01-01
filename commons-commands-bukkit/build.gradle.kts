plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "StrokkCommands integration for 6b6t Bukkit/Paper plugins"

dependencies {
    api(project(":commons-commands-core"))

    compileOnly(libs.paper.api)
    compileOnly(libs.strokkcommands.annotations.paper)
    annotationProcessor(libs.strokkcommands.processor.paper)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
