/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiHigherOrderArgumentBuilder
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression

class RuiHigherOrderArgument(
    ruiClass: RuiClass,
    val index: Int,
    val value: IrFunctionExpression,
    dependencies: List<RuiStateVariable>,
    val implicitClass: RuiClass
) : RuiExpression(ruiClass, value, RuiExpressionOrigin.HIGHER_ORDER_ARGUMENT, dependencies) {

    /**
     * Parameters of the parameter function, these are state variables of the implicit
     * component (in addition to the state variables of the start and intermediate scopes).
     */
    val valueParameters = value.function.valueParameters

    val builder = RuiHigherOrderArgumentBuilder(ruiClass.builder, this)

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitHigherOrderArgument(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) = Unit
}