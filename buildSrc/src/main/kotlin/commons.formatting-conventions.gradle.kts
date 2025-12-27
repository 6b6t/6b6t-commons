plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        targetExclude("build/generated/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()

        palantirJavaFormat()

        importOrder("", "java|javax", "\\#")
    }
}
