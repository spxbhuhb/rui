/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.plugin

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.ir2rum.OriginalFunctionTransform
import hu.simplexion.rui.kotlin.plugin.ir.sir2ir.Sir2IrTransform
import hu.simplexion.rui.runtime.Plugin.PLUGIN_ID
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.dump

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

            OriginalFunctionTransform(this).also {
                moduleFragment.accept(it, null)
                // this check prevents the plugin to go on if there is an error that would prevent
                // generation of a correct IR tree
                if (!compilationError) {
                    Sir2IrTransform(this, it.rumClasses, it.ruiEntryPoints).transform()
                }
            }

            RuiDumpPoint.RuiTree.dump(this) {
                output("RUI CLASSES", rumClasses.values.joinToString("\n\n") { it.dump() })
            }

            RuiDumpPoint.After.dump(this) {
                output("DUMP AFTER", moduleFragment.dump())
            }

        }
    }
}

