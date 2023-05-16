/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.toir

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_INTERNAL_PLUGIN_ERROR
import hu.simplexion.rui.kotlin.plugin.ir.model.RuiClass
import hu.simplexion.rui.kotlin.plugin.ir.model.RuiEntryPoint
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.psi.KtModifierListOwner

class RuiToIrTransform(
    private val ruiContext: RuiPluginContext,
    val ruiClasses: List<RuiClass>,
    val ruiEntryPoints: List<RuiEntryPoint>
) : IrElementTransformerVoidWithContext(), RuiAnnotationBasedExtension {

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        ruiContext.annotations

    fun transform() {

        ruiClasses.forEach {
            try {
                it.builder.build()
            } catch (ex: Exception) {
                RUI_IR_INTERNAL_PLUGIN_ERROR.report(ruiContext, it.irFunction, ex.stackTraceToString())
            }
            it.irFunction.file.addChild(it.irClass)
        }

        ruiEntryPoints.forEach {
            it.builder.build()
        }
    }
}
