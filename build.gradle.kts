import java.io.RandomAccessFile

plugins {
    java
    `maven-publish`
}

group = "org.glavo"
version = "2.1"

java {
    withSourcesJar()
}

tasks.compileJava {
    options.release.set(9)

    doLast {
        val tree = fileTree(destinationDirectory)
        tree.include("**/*.class")
        tree.exclude("module-info.class")
        tree.forEach {
            RandomAccessFile(it, "rw").use { rf ->
                rf.seek(7)   // major version
                rf.write(51)   // java 7
                rf.close()
            }
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.glavo.rcon.Rcon"
        )
    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = project.name
            from(components["java"])

            pom {
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("glavo")
                        name.set("Glavo")
                        email.set("zjx001202@gmail.com")
                    }
                }
            }
        }
    }
}
