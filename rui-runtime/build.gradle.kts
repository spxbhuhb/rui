/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    kotlin("multiplatform")
    id("com.github.gmazzo.buildconfig")
    signing
    `maven-publish`
}

val publishSnapshotUrl = (System.getenv("RUI_PUBLISH_SNAPSHOT_URL") ?: project.findProperty("rui.publish.snapshot.url"))?.toString()
val publishReleaseUrl = (System.getenv("RUI_PUBLISH_RELEASE_URL") ?: project.findProperty("rui.publish.release.url"))?.toString()
val publishUsername = (System.getenv("RUI_PUBLISH_USERNAME") ?: project.findProperty("rui.publish.username"))?.toString()
val publishPassword = (System.getenv("RUI_PUBLISH_PASSWORD") ?: project.findProperty("rui.publish.password"))?.toString()
val isSnapshot = "SNAPSHOT" in project.version.toString()
val isPublishing = (System.getenv("RUI_PUBLISH") ?: project.findProperty("rui.publish")?.toString().toBoolean()) == true

kotlin {

    jvm {
        jvmToolchain(11)
    }

    js(IR) {
        browser()
        nodejs()
        binaries.library()
    }

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(kotlin("test-junit"))
            }
        }
    }
}

buildConfig {
    packageName("hu.simplexion.rui.runtime")
    buildConfigField("String", "PLUGIN_VERSION", "\"${project.version}\"")
}

val baseName = "rui-runtime"
val pomName = "Rui Runtime"
val scmPath = "spxbhuhb/rui"

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

signing {
    if (project.properties["signing.keyId"] == null) {
        useGpgCmd()
    }
    sign(publishing.publications)
}

publishing {

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

        artifact(javadocJar.get())

        pom {
            description.set("Runtime of Rui, a Kotlin reactive UI library inspired by Svelte.")
            name.set(pomName)
            url.set("https://github.com/$scmPath")
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