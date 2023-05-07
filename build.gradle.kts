/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("multiplatform") version "1.8.20" apply false
    id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
    id("com.gradle.plugin-publish") version "1.1.0" apply false
    signing
    `maven-publish`
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
        classpath("com.android.tools.build:gradle:7.0.4")
    }
}

allprojects {
    group = "hu.simplexion.rui"
    version = "0.1.0"
}

subprojects {

    repositories {
        mavenCentral()
        google()
    }

}
