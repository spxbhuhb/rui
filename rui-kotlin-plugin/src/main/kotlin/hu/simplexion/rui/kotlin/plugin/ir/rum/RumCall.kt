/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.RUI_CALL
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.toRuiClassFqName
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiCallBuilder
import org.jetbrains.kotlin.ir.expressions.IrCall

open class RumCall(
    rumClass: RumClass,
    index: Int,
    val irCall: IrCall
) : RumRenderingStatement(rumClass, index) {

    override val name = "$RUI_CALL$index"

    val target = irCall.symbol.owner.toRuiClassFqName(rumClass.ruiContext)

    val valueArguments = mutableListOf<RumExpression>()

    override val builder = RuiCallBuilder(rumClass.builder, this)

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitCall(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) {
        valueArguments.forEach { it.accept(visitor, data) }
    }
}