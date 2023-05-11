/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.model

import hu.simplexion.rui.kotlin.plugin.RUI_STATE_VARIABLE_LIMIT
import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui.RUI_IR_TOO_MANY_STATE_VARIABLES
import hu.simplexion.rui.kotlin.plugin.transform.builders.RuiStateVariableBuilder
import hu.simplexion.rui.kotlin.plugin.util.RuiElementVisitor
import org.jetbrains.kotlin.ir.declarations.IrValueParameter

class RuiExternalStateVariable(
    override val ruiClass: RuiClass,
    override val index: Int,
    val irValueParameter: IrValueParameter
) : RuiStateVariable {

    override val originalName = irValueParameter.name.identifier
    override val name = irValueParameter.name

    override val builder = RuiStateVariableBuilder.builderFor(ruiClass.builder, this)

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitExternalStateVariable(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) = Unit

    init {
        RUI_IR_TOO_MANY_STATE_VARIABLES.check(ruiClass, irValueParameter) { index <= RUI_STATE_VARIABLE_LIMIT }
    }

}