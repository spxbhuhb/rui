/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin

import com.google.auto.service.AutoService
import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.RuiConfigurationKeys.ANNOTATION
import hu.simplexion.rui.kotlin.plugin.ir.RuiConfigurationKeys.DUMP
import hu.simplexion.rui.kotlin.plugin.ir.RuiConfigurationKeys.EXPORT_STATE
import hu.simplexion.rui.kotlin.plugin.ir.RuiConfigurationKeys.IMPORT_STATE
import hu.simplexion.rui.kotlin.plugin.ir.RuiConfigurationKeys.PLUGIN_LOG_DIR
import hu.simplexion.rui.kotlin.plugin.ir.RuiConfigurationKeys.PRINT_DUMPS
import hu.simplexion.rui.kotlin.plugin.ir.RuiConfigurationKeys.ROOT_NAME_STRATEGY
import hu.simplexion.rui.kotlin.plugin.ir.RuiConfigurationKeys.TRACE
import hu.simplexion.rui.runtime.Plugin.RUI_ANNOTATION
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor

/**
 * Registers the extensions into the compiler.
 */
@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class RuiCompilerPluginRegistrar(
    val dumpPoints: List<RuiDumpPoint> = emptyList(),
    val rootNameStrategy: RuiRootNameStrategy = RuiRootNameStrategy.StartOffset,
    val trace: Boolean = false,
    val exportState: Boolean = false,
    val importState: Boolean = false,
    val printDumps: Boolean = false,
    val pluginLogDir: String? = null
) : CompilerPluginRegistrar() {

    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val options = RuiOptions(
            annotations = configuration.get(ANNOTATION).let { if (! it.isNullOrEmpty()) it else listOf(RUI_ANNOTATION) },
            configuration.get(DUMP) ?: dumpPoints,
            configuration.get(ROOT_NAME_STRATEGY) ?: rootNameStrategy,
            configuration.get(TRACE) ?: trace,
            configuration.get(EXPORT_STATE) ?: exportState,
            configuration.get(IMPORT_STATE) ?: importState,
            configuration.get(PRINT_DUMPS) ?: printDumps,
            configuration.get(PLUGIN_LOG_DIR) ?: pluginLogDir
        )
        registerComponents(options, configuration.getBoolean(JVMConfigurationKeys.IR))
    }

    fun ExtensionStorage.registerComponents(options: RuiOptions, useIr: Boolean) {

        StorageComponentContainerContributor.registerExtension(
            RuiComponentContainerContributor(options.annotations, useIr)
        )

        IrGenerationExtension.registerExtension(
            RuiGenerationExtension(options)
        )

    }

}
