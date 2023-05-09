/*
 * Copyright (C) 2020 Brian Norman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.simplexion.rui.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Settings for the Rui compiler plugin.
 */
open class RuiGradleExtension(objects: ObjectFactory) {

    /**
     * ```
     * Category: Source code option
     * ```
     *
     * Name of the annotation that instructs the plugin to treat a function as a Rui
     * function.
     *
     * Default: `hu.simplexion.rui.runtime.Rui`
     */
    val annotations: ListProperty<String> = objects.listProperty(String::class.java)

    /**
     * ```
     * Category: Functionality of generated code
     * ```
     *
     * When `true` the plugin generates an `exportState` function for each rui component. This function
     * exports the state of the component to be loaded later with `importState` or analyzed by other
     * tools. See the export/import section of the documentation for more information.
     *
     * Default: `false`
     */
    val exportState: Property<Boolean> = objects.property(Boolean::class.java).also { it.set(false) }

    /**
     * ```
     * Category: Functionality of generated code
     * ```
     *
     * When `true` the plugin generates an `importState` function for each rui component. This function
     * imports the state of the component exported with `exportState`. This might be used to restore
     * the UI into a saved state.
     *
     * Default: `false`
     */
    val importState: Property<Boolean> = objects.property(Boolean::class.java).also { it.set(false) }

    /**
     * ```
     * Category: Debug of generated code
     * ```
     *
     * When `true` the plugin adds trace to the generated code. These traces contains all Rui related
     * function calls and state variables. See the troubleshooting section of the documentation for
     * more information.
     * ```
     *
     * Default: `false`
     */
    val trace: Property<Boolean> = objects.property(Boolean::class.java).also { it.set(false) }

    /**
     * ```
     * Category: Code generation
     * ```
     *
     * Naming strategy for the rui entry point classes (generated for `rui` function calls).
     * Possible values:
     *
     * - `no-postfix` - entry point class will be `RuiRoot`, used for unit tests
     * - `start-offset` - entry point class will be `RuiRoot<name-of-source-file><start-offset>` like `RuiRootMain234`
     *
     * Default: `start-offset`
     */
    val rootNameStrategy: Property<String> = objects.property(String::class.java).also { it.set("start-offset") }

    /**
     * ```
     * Category: Plugin development
     * ```
     *
     * Points of plugin where it dumps information about the compilation (such as the IR tree, Rui tree etc.)
     *
     * ```
     * dumpPoints.set(listOf("before", "after", "rui-tree", "kotlin-like"))
     * ```
     */
    val dumpPoints: ListProperty<String> = objects.listProperty(String::class.java)

    /**
     * ```
     * Category: Plugin development
     * ```
     *
     * When `true` the plugin prints dumps specified by [dumpPoints] to the standard output instead passing them
     * to the compiler framework.
     */
    val printDumps: Property<Boolean> = objects.property(Boolean::class.java).also { it.set(false) }

    /**
     * ```
     * Category: Plugin development/troubleshooting.
     * ```
     *
     * When set the plugin saves logs into a file named "rui-log-yyyyMMdd-HHmmss.txt" file. This is mostly
     * useful during the development of the plugin itself and/or troubleshooting. The file contains
     * the dumps specified by [dumpPoints].
     *
     * Generates a compilation warning when set because of the large amount of data it can save.
     *
     * Relative paths save the data into the gradle daemons log directory. On my machine it is:
     *
     * `/Users/<username>/Library/Application Support/kotlin/daemon`
     */
    val pluginLogDir: Property<String?> = objects.property(String::class.java).also { it.set(null as String?) }
}

@Suppress("unused")
fun org.gradle.api.Project.rui(configure: Action<RuiGradleExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("rui", configure)
