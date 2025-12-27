plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Command framework for 6b6t Velocity plugins"

dependencies {
    api(project(":commons-command-core"))

    compileOnly(libs.velocity.api)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
