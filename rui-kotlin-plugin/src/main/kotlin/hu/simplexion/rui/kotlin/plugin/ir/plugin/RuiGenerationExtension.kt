/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.plugin

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.ir2rum.OriginalFunctionTransform
import hu.simplexion.rui.runtime.Plugin.PLUGIN_ID
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.file

internal class RuiGenerationExtension(
    val options: RuiOptions
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        RuiPluginContext(
            pluginContext,
            options,
            pluginContext.createDiagnosticReporter(PLUGIN_ID),
            moduleFragment
        ).apply {

            // --------  preparations  --------

            pluginLogDir?.let {
                diagnosticReporter.report(
                    IrMessageLogger.Severity.WARNING,
                    "rui.pluginLogDir is set to: $it",
                    IrMessageLogger.Location(moduleFragment.name.asString(), 1, 1)
                )
            }

            RuiDumpPoint.Before.dump(this) {
                output("DUMP BEFORE", moduleFragment.dump())
            }

            // --------  IR to RUM  --------

            val ir2Rum = OriginalFunctionTransform(this)

            moduleFragment.accept(ir2Rum, null)

            if (compilationError) return // prevent the plugin to go on if there is an error that would result in an incorrect IR tree

            RuiDumpPoint.RuiTree.dump(this) {
                output("RUI CLASSES", ir2Rum.rumClasses.joinToString("\n\n") { it.dump() })
            }

            // --------  RUM to AIR  --------

            val airClasses = ir2Rum.rumClasses.map { it.toAir(this) }
            val airEntryPoints = ir2Rum.rumEntryPoints.map { it.toAir(this) }

            // --------  AIR to IR  --------

            airClasses.forEach { it.rumElement.originalFunction.file.addChild(it.toIr(this)) }
            airEntryPoints.forEach { it.toIr(this) }

            // --------  finishing up  --------

            RuiDumpPoint.After.dump(this) {
                output("DUMP AFTER", moduleFragment.dump())
            }

        }
    }
}

