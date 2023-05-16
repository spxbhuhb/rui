/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.RUI_WHEN
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiWhenBuilder
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrWhen

class RuiWhen(
    ruiClass: RuiClass,
    index: Int,
    val irSubject: IrVariable?,
    val irWhen: IrWhen
) : RuiStatement(ruiClass, index) {

    override val name = "$RUI_WHEN$index"

    val branches = mutableListOf<RuiBranch>()

    override val builder = RuiWhenBuilder(ruiClass.builder, this)

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitWhen(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) {
        branches.forEach { it.accept(visitor, data) }
    }
}