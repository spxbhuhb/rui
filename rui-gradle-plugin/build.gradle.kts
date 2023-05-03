/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("org.jetbrains.dokka")
    signing
    `maven-publish`
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    implementation(project(":rui-runtime"))
}

gradlePlugin {
    plugins {
        create("ruiGradlePlugin") {
            id = "rui"
            displayName = "RUI Plugin"
            description = "RUI Plugin"
            implementationClass = "hu.simplexion.rui.gradle.plugin.RuiGradlePlugin"
        }
    }
}
