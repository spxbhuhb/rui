/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    kotlin("multiplatform") version "1.8.20"
    id("hu.simplexion.rui") version "0.1.0-SNAPSHOT"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

rui {
    dumpPoints.set(listOf("before", "after", "rui-tree", "kotlin-like"))
    trace.set(false)
    exportState.set(true)
    importState.set(true)
    //pluginLogDir.set("$projectDir/tmp/log")
}

kotlin {
    jvm {}
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("hu.simplexion.rui:rui-runtime:0.1.0-SNAPSHOT")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}