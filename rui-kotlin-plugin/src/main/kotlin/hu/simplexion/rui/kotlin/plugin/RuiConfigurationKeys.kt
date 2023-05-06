/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package hu.simplexion.rui.kotlin.plugin

import hu.simplexion.rui.runtime.Plugin.OPTION_NAME_ANNOTATION
import hu.simplexion.rui.runtime.Plugin.OPTION_NAME_DUMP_POINT
import hu.simplexion.rui.runtime.Plugin.OPTION_NAME_EXPORT_STATE
import hu.simplexion.rui.runtime.Plugin.OPTION_NAME_IMPORT_STATE
import hu.simplexion.rui.runtime.Plugin.OPTION_NAME_PLUGIN_LOG_DIR
import hu.simplexion.rui.runtime.Plugin.OPTION_NAME_ROOT_NAME_STRATEGY
import hu.simplexion.rui.runtime.Plugin.OPTION_NAME_TRACE
import hu.simplexion.rui.runtime.Plugin.OPTION_NAME_UNIT_TEST_MODE
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object RuiConfigurationKeys {
    val ANNOTATION: CompilerConfigurationKey<List<String>> = CompilerConfigurationKey.create(OPTION_NAME_ANNOTATION)
    val DUMP: CompilerConfigurationKey<List<RuiDumpPoint>> = CompilerConfigurationKey.create(OPTION_NAME_DUMP_POINT)
    val ROOT_NAME_STRATEGY: CompilerConfigurationKey<RuiRootNameStrategy> = CompilerConfigurationKey.create(OPTION_NAME_ROOT_NAME_STRATEGY)
    val TRACE: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey.create(OPTION_NAME_TRACE)
    val EXPORT_STATE: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey.create(OPTION_NAME_EXPORT_STATE)
    val IMPORT_STATE: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey.create(OPTION_NAME_IMPORT_STATE)
    val UNIT_TEST_MODE: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey.create(OPTION_NAME_UNIT_TEST_MODE)
    val PLUGIN_LOG_DIR: CompilerConfigurationKey<String> = CompilerConfigurationKey.create(OPTION_NAME_PLUGIN_LOG_DIR)
}

