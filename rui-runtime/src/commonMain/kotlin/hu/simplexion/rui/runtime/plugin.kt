/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime

object Plugin {
    const val OPTION_NAME_ANNOTATION = "annotation"
    const val OPTION_NAME_DUMP_POINT = "dump-point"
    const val OPTION_NAME_ROOT_NAME_STRATEGY = "root-name-strategy"
    const val OPTION_NAME_TRACE = "trace"
    const val OPTION_NAME_EXPORT_STATE = "export-state"
    const val OPTION_NAME_IMPORT_STATE = "import-state"
    const val OPTION_NAME_UNIT_TEST_MODE = "unit-test-mode"
    const val OPTION_NAME_PLUGIN_LOG_DIR = "plugin-log-dir"

    const val PLUGIN_ID = "rui"
    const val PLUGIN_GROUP = "hu.simplexion.rui"
    const val PLUGIN_VERSION = BuildConfig.PLUGIN_VERSION

    const val GRADLE_PLUGIN_NAME = "rui-gradle-plugin"
    const val GRADLE_EXTENSION_NAME = "rui"

    const val KOTLIN_PLUGIN_NAME = "rui-kotlin-plugin"

    const val RUNTIME_NAME = "rui-runtime"

    const val RUI_ANNOTATION = "hu.simplexion.rui.runtime.Rui"

    val RUI_FRAGMENT_CLASS = listOf("hu", "simplexion", "rui", "runtime", "RuiFragment")
    val RUI_ADAPTER_CLASS = listOf("hu", "simplexion", "rui", "runtime", "RuiAdapter")
    val RUI_BRIDGE_CLASS = listOf("hu", "simplexion", "rui", "runtime", "RuiBridge")
    val RUI_BLOCK_CLASS = listOf("hu", "simplexion", "rui", "runtime", "RuiBlock")
    val RUI_WHEN_CLASS = listOf("hu", "simplexion", "rui", "runtime", "RuiWhen")
    val RUI_FOR_LOOP_CLASS = listOf("hu", "simplexion", "rui", "runtime", "RuiForLoop")
    val RUI_ENTRY_FUNCTION = listOf("hu", "simplexion", "rui", "runtime", "rui")


}