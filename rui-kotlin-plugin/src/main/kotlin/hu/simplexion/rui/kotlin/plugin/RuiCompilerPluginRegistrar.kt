/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin

import com.google.auto.service.AutoService
import hu.simplexion.rui.kotlin.plugin.RuiConfigurationKeys.ANNOTATION
import hu.simplexion.rui.kotlin.plugin.RuiConfigurationKeys.DUMP
import hu.simplexion.rui.kotlin.plugin.RuiConfigurationKeys.EXPORT_STATE
import hu.simplexion.rui.kotlin.plugin.RuiConfigurationKeys.IMPORT_STATE
import hu.simplexion.rui.kotlin.plugin.RuiConfigurationKeys.ROOT_NAME_STRATEGY
import hu.simplexion.rui.kotlin.plugin.RuiConfigurationKeys.TRACE
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
    val importState: Boolean = false
) : CompilerPluginRegistrar() {

    override val supportsK2 = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val annotations = configuration.get(ANNOTATION).let { if (!it.isNullOrEmpty()) it else listOf(RUI_ANNOTATION) }
        val dumpPoints = configuration.get(DUMP) ?: dumpPoints
        val rootNameStrategy = configuration.get(ROOT_NAME_STRATEGY) ?: rootNameStrategy
        val trace = configuration.get(TRACE) ?: trace
        val exportState = configuration.get(EXPORT_STATE) ?: exportState
        val importState = configuration.get(IMPORT_STATE) ?: importState

        val options = RuiOptions(annotations, dumpPoints, rootNameStrategy, trace, exportState, importState)

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

    companion object {
        fun withAll() =
            RuiCompilerPluginRegistrar(
                RuiDumpPoint.values().toList(),
                RuiRootNameStrategy.NoPostfix,
                trace = true,
                exportState = true,
                importState = true
            )
    }

}
