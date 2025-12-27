plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Database utilities for 6b6t plugins - MariaDB connection management"

dependencies {
    api(projects.commonsConfig)
    api(libs.mariadb.java.client)
    api(libs.hikaricp)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.jspecify)
}
