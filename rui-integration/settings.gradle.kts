/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
rootProject.name = "rui-integration"

pluginManagement {
//    resolutionStrategy {
//        eachPlugin {
//            if (requested.id.id == "rui") {
//                useModule("hu.simplexion.rui:rui-gradle-plugin:0.1.0-SNAPSHOT")
//            }
//        }
//    }
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}