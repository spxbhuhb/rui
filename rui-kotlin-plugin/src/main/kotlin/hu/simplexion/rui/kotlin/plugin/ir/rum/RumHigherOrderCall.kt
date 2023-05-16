/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.RUI_HIGHER_ORDER_CALL
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiHigherOrderCallBuilder
import org.jetbrains.kotlin.ir.expressions.IrCall

class RumHigherOrderCall(
    rumClass: RumClass,
    index: Int,
    irCall: IrCall
) : RumCall(rumClass, index, irCall) {

    override val name = "$RUI_HIGHER_ORDER_CALL$index"

    override val builder = RuiHigherOrderCallBuilder(rumClass.builder, this)

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitHigherOrderCall(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) {
        valueArguments.forEach { it.accept(visitor, data) }
    }
}