plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        targetExclude("build/generated/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()

        palantirJavaFormat("2.82.0")

        importOrder("", "java|javax", "\\#")
    }
}
