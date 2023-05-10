/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    id("com.gradle.plugin-publish")
    kotlin("jvm")
    signing
    `maven-publish`
}

val String.propValue
    get() = (System.getenv(this.toUpperCase().replace('.', '_')) ?: project.findProperty(this))?.toString() ?: ""

val isPublishing = "rui.publish".propValue
val publishSnapshotUrl = "rui.publish.snapshot.url".propValue
val publishReleaseUrl = "rui.publish.release.url".propValue
val publishUsername = "rui.publish.username".propValue
val publishPassword = "rui.publish.password".propValue
val isSnapshot = "SNAPSHOT" in project.version.toString()

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    implementation(project(":rui-runtime"))
}

gradlePlugin {
    website.set("https://github.com/spxbhuhb/rui")
    vcsUrl.set("https://github.com/spxbhuhb/rui.git")
    plugins {
        create("rui") {
            id = "hu.simplexion.rui"
            displayName = "Rui Gradle Plugin"
            description = "Gradle plugin of Rui, a Kotlin reactive UI library inspired by Svelte."
            implementationClass = "hu.simplexion.rui.gradle.plugin.RuiGradlePlugin"
            tags.set(listOf("kotlin", "rui", "reactive"))
        }
    }
}

signing {
    if (project.properties["signing.keyId"] == null) {
        useGpgCmd()
    }
    sign(publishing.publications)
}

publishing {

    val scmPath = "spxbhuhb/rui"
    val pomName = "Rui Gradle Plugin"

    repositories {
        maven {
            name = "MavenCentral"
            url = project.uri(requireNotNull(if (isSnapshot) publishSnapshotUrl else publishReleaseUrl))
            credentials {
                username = publishUsername
                password = publishPassword
            }
        }
    }

    publications.withType<MavenPublication>().all {
        pom {
            url.set("https://github.com/$scmPath")
            name.set(pomName)
            description.set("Gradle plugin of Rui, a Kotlin reactive UI library inspired by Svelte.")
            scm {
                url.set("https://github.com/$scmPath")
                connection.set("scm:git:git://github.com/$scmPath.git")
                developerConnection.set("scm:git:ssh://git@github.com/$scmPath.git")
            }
            licenses {
                license {
                    name.set("Apache 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("toth-istvan-zoltan")
                    name.set("Tóth István Zoltán")
                    url.set("https://github.com/toth-istvan-zoltan")
                    organization.set("Simplexion Kft.")
                    organizationUrl.set("https://www.simplexion.hu")
                }
            }
        }
    }
}