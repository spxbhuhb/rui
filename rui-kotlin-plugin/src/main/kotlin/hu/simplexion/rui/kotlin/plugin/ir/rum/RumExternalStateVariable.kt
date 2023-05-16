/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.RUI_STATE_VARIABLE_LIMIT
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_TOO_MANY_STATE_VARIABLES
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiStateVariableBuilder
import org.jetbrains.kotlin.ir.declarations.IrValueParameter

class RumExternalStateVariable(
    override val rumClass: RumClass,
    override val index: Int,
    val irValueParameter: IrValueParameter
) : RumStateVariable {

    override val originalName = irValueParameter.name.identifier
    override val name = irValueParameter.name

    override val builder = RuiStateVariableBuilder.builderFor(rumClass.builder, this)

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitExternalStateVariable(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) = Unit

    init {
        RUI_IR_TOO_MANY_STATE_VARIABLES.check(rumClass, irValueParameter) { index <= RUI_STATE_VARIABLE_LIMIT }
    }

}