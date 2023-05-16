/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import org.jetbrains.kotlin.ir.expressions.IrExpression

class RumValueArgument(
    rumClass: RumClass,
    val index: Int,
    val value: IrExpression,
    dependencies: List<RumStateVariable>
) : RumExpression(rumClass, value, RumExpressionOrigin.VALUE_ARGUMENT, dependencies) {

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitValueArgument(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) = Unit
}