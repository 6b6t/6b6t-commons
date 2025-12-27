plugins {
    `java-library`
    `maven-publish`
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/6b6t/6b6t-commons")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description ?: "Common utilities for 6b6t plugins")
                url.set("https://github.com/6b6t/6b6t-commons")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("6b6t")
                        name.set("6b6t Team")
                        url.set("https://github.com/6b6t")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/6b6t/6b6t-commons.git")
                    developerConnection.set("scm:git:ssh://github.com/6b6t/6b6t-commons.git")
                    url.set("https://github.com/6b6t/6b6t-commons")
                }
            }
        }
    }
}
