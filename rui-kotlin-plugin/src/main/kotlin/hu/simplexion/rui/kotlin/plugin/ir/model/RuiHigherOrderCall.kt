/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.RUI_HIGHER_ORDER_CALL
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiHigherOrderCallBuilder
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor
import org.jetbrains.kotlin.ir.expressions.IrCall

class RuiHigherOrderCall(
    ruiClass: RuiClass,
    index: Int,
    irCall: IrCall
) : RuiCall(ruiClass, index, irCall) {

    override val name = "$RUI_HIGHER_ORDER_CALL$index"

    override val builder = RuiHigherOrderCallBuilder(ruiClass.builder, this)

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitHigherOrderCall(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) {
        valueArguments.forEach { it.accept(visitor, data) }
    }
}