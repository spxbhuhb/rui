/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.sir2ir

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_INTERNAL_PLUGIN_ERROR
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumEntryPoint
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.psi.KtModifierListOwner

class Sir2IrTransform(
    private val ruiContext: RuiPluginContext,
    val rumClasses: List<RumClass>,
    val ruiEntryPoints: List<RumEntryPoint>
) : IrElementTransformerVoidWithContext(), RuiAnnotationBasedExtension {

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        ruiContext.annotations

    fun transform() {

        rumClasses.forEach {
            try {
                it.builder.build()
            } catch (ex: Exception) {
                RUI_IR_INTERNAL_PLUGIN_ERROR.report(ruiContext, it.originalFunction, ex.stackTraceToString())
            }
            it.originalFunction.file.addChild(it.irClass)
        }

        ruiEntryPoints.forEach {
            it.builder.build()
        }
    }
}
