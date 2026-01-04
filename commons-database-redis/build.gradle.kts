plugins {
    id("commons.java-conventions")
    id("commons.publishing-conventions")
}

description = "Redis database utilities for 6b6t plugins"

dependencies {
    api(projects.commonsConfig)
    api(libs.jedis)
}
