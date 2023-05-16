/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.RUI_BLOCK
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiBlockBuilder
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor
import org.jetbrains.kotlin.ir.expressions.IrBlock

class RuiBlock(
    ruiClass: RuiClass,
    index: Int,
    val irBlock: IrBlock
) : RuiStatement(ruiClass, index) {

    override val name = "$RUI_BLOCK$index"

    val statements = mutableListOf<RuiStatement>()

    override val builder = RuiBlockBuilder(ruiClass.builder, this)

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitBlock(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) {
        statements.forEach { it.accept(visitor, data) }
    }
}