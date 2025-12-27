plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Command framework for 6b6t Bukkit/Paper plugins"

dependencies {
    api(project(":commons-command-core"))

    compileOnly(libs.paper.api)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
