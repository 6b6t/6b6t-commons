plugins {
    base
}

allprojects {
    group = "net.blockhost.commons"
    version = "1.0.0-SNAPSHOT"

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "PaperMC Repository"
        }
        maven("https://eldonexus.de/repository/maven-public/") {
            name = "Eldonexus"
            mavenContent {
                releasesOnly()
            }
        }
        maven("https://eldonexus.de/repository/maven-snapshots/") {
            name = "Eldonexus Snapshots"
            mavenContent {
                snapshotsOnly()
            }
        }
        mavenCentral()
    }
}

// Aggregated Javadoc task for all modules
tasks.register<Javadoc>("aggregateJavadoc") {
    group = "documentation"
    description = "Generates aggregated Javadoc for all modules"

    val javadocTasks = subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("javadoc") as? Javadoc
    }

    dependsOn(javadocTasks)

    source(javadocTasks.map { it.source })
    classpath = files(javadocTasks.map { it.classpath })

    setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)

    options {
        this as StandardJavadocDocletOptions
        encoding = "UTF-8"
        charSet = "UTF-8"
        links("https://docs.oracle.com/en/java/javase/25/docs/api/")
        links("https://jd.papermc.io/paper/1.21.4/")
        links("https://jd.advntr.dev/api/4.18.0/")
        links("https://javadoc.io/doc/com.zaxxer/HikariCP/6.2.1/")
        addStringOption("Xdoclint:none", "-quiet")

        // Add a nice header
        windowTitle = "6b6t Commons API"
        docTitle = "6b6t Commons API Documentation"
        header = "<b>6b6t Commons</b>"
        bottom = "Copyright &#169; 2024 6b6t. All rights reserved."
    }
}
