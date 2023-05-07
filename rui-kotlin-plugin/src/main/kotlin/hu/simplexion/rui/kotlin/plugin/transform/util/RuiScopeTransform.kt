/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.util

import hu.simplexion.rui.kotlin.plugin.model.RuiClass
import hu.simplexion.rui.kotlin.plugin.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.psi.KtModifierListOwner

/**
 * Transforms the expressions that calculates the state variables from class scope
 * to function scope. This is necessary for the external patch functions, when
 * they recalculate the value of the variable.
 *
 * Note, that this transform is preceded by `RuiStateTransform` which transforms function
 * variable access to class variable access first.
 *
 * Maps from this:
 *
 * ```
 * $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiBlock declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiBlock' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiBlock origin=null
 * ```
 *
 * to this:
 *
 * ```
 * $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.get.RuiBlock declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiBlock.ruiEp1' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiBlock origin=null
 * ```
 *
 * Notice the added `.ruiEp1` in the second line.
 */
class RuiScopeTransform(
    private val ruiClass: RuiClass,
    private val newScope: IrValueSymbol
) : IrElementTransformerVoidWithContext(), RuiAnnotationBasedExtension {

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        ruiClass.ruiContext.annotations

    override fun visitGetValue(expression: IrGetValue): IrExpression {
        return if (expression.symbol == ruiClass.irClass.thisReceiver!!.symbol) {
            with(expression) {
                IrGetValueImpl(startOffset, endOffset, type, newScope, origin)
            }
        } else {
            super.visitGetValue(expression)
        }
    }
}
