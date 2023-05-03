/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    kotlin("jvm")
    kotlin("kapt")
    signing
    `maven-publish`
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    implementation("com.google.auto.service:auto-service-annotations:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")

    implementation(project(":rui-runtime"))

    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "hu.simplexion.rui"
            artifactId = "rui-kotlin-plugin"
            version = "0.1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}

tasks.test {
    testLogging.showStandardStreams = true
}