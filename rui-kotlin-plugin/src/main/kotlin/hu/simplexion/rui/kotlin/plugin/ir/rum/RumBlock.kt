/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.RUI_BLOCK
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiBlockBuilder
import org.jetbrains.kotlin.ir.expressions.IrBlock

class RumBlock(
    rumClass: RumClass,
    index: Int,
    val irBlock: IrBlock
) : RumRenderingStatement(rumClass, index) {

    override val name = "$RUI_BLOCK$index"

    val statements = mutableListOf<RumRenderingStatement>()

    override val builder = RuiBlockBuilder(rumClass.builder, this)

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitBlock(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) {
        statements.forEach { it.accept(visitor, data) }
    }
}