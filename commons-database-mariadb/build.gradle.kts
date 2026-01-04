plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "MariaDB database utilities for 6b6t plugins"

dependencies {
    api(projects.commonsDatabaseCore)
    api(libs.mariadb.java.client)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
