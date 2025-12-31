enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "4.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "server-commons"

develocity {
    buildScan {
        val isCi = !System.getenv("CI").isNullOrEmpty()
        if (isCi) {
            termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
            termsOfUseAgree = "yes"
            tag("CI")
        }
        publishing.onlyIf { isCi }
    }
}

include("commons-core")
include("commons-database")
include("commons-config")
include("commons-message")
include("commons-command-core")
include("commons-command-bukkit")
include("commons-command-velocity")
