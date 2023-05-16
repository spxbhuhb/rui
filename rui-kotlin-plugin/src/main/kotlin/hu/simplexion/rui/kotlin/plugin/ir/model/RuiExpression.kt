/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor
import org.jetbrains.kotlin.ir.expressions.IrExpression

open class RuiExpression(
    val ruiClass: RuiClass,
    var irExpression: IrExpression,
    val origin: RuiExpressionOrigin,
    val dependencies: List<RuiStateVariable>
) : RuiElement {

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitExpression(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) = Unit

}