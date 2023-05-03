/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("com.github.gmazzo.buildconfig")
    signing
    `maven-publish`
}

kotlin {

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
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