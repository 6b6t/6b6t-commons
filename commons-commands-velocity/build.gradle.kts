plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "StrokkCommands integration for 6b6t Velocity plugins"

dependencies {
    api(project(":commons-commands-core"))

    compileOnly(libs.velocity.api)
    compileOnly(libs.strokkcommands.annotations.velocity)
    annotationProcessor(libs.strokkcommands.processor.velocity)
}
