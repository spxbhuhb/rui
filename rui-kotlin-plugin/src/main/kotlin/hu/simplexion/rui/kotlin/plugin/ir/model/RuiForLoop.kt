/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.RUI_FOR_LOOP
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiFragmentBuilder
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor
import org.jetbrains.kotlin.ir.expressions.IrBlock

class RuiForLoop(
    ruiClass: RuiClass,
    index: Int,
    val irBlock: IrBlock,
    var iterator: RuiDeclaration,
    val condition: RuiExpression,
    val loopVariable: RuiDeclaration,
    val body: RuiStatement,
) : RuiStatement(ruiClass, index) {

    override val name = "$RUI_FOR_LOOP$index"

    override val builder: RuiFragmentBuilder
        get() = TODO("Not yet implemented")

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitForLoop(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) {
        iterator.accept(visitor, data)
        condition.accept(visitor, data)
        loopVariable.accept(visitor, data)
        body.accept(visitor, data)
    }
}