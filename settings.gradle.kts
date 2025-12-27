enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "server-commons"

include("commons-core")
include("commons-database")
include("commons-config")
include("commons-message")
include("commons-command-core")
include("commons-command-bukkit")
include("commons-command-velocity")
