plugins {
    id("java-library")
    id("org.glavo.compile-module-info-plugin") version "2.0"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.glavo.load-maven-publish-properties") version "0.1.0"
}

group = "org.glavo"
version = "3.0" + "-SNAPSHOT"
description = "A Java RCON client"

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.compileJava {
    options.release.set(8)
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
                        name.set("MIT")
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

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

if (rootProject.ext.has("signing.key")) {
    signing {
        useInMemoryPgpKeys(
            rootProject.ext["signing.keyId"].toString(),
            rootProject.ext["signing.key"].toString(),
            rootProject.ext["signing.password"].toString(),
        )
        sign(publishing.publications["maven"])
    }
}

nexusPublishing {
    repositories {
        sonatype {
            stagingProfileId.set(rootProject.ext["sonatypeStagingProfileId"].toString())
            username.set(rootProject.ext["sonatypeUsername"].toString())
            password.set(rootProject.ext["sonatypePassword"].toString())
        }
    }
}
