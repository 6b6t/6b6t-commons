plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Command framework for 6b6t plugins - SubCommand pattern"

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
