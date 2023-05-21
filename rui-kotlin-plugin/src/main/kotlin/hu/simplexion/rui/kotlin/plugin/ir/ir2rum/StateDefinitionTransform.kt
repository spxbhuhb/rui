/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.ir2rum

import hu.simplexion.rui.kotlin.plugin.ir.RUI_STATE_VARIABLE_LIMIT
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_RENDERING_VARIABLE
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_STATE_VARIABLE_SHADOW
import hu.simplexion.rui.kotlin.plugin.ir.rum.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable

/**
 * Transforms function parameters and top-level variable declarations into
 * state variables.
 *
 * @property  skipParameters  Skip the first N parameter of the original function when
 *                            converting the function parameters into external state variables.
 *                            Used for entry points when the first parameter is the adapter.
 */
class StateDefinitionTransform(
    private val rumClass: RumClass,
    private val skipParameters: Int
) {

    val names = mutableListOf<String>()

    var stateVariableIndex = 0

    fun transform() {

        rumClass.originalFunction.valueParameters.forEachIndexed { index, valueParameter ->

            if (index < skipParameters) return@forEachIndexed

            RumExternalStateVariable(rumClass, stateVariableIndex, valueParameter).register(valueParameter)
        }

        rumClass.originalStatements.forEach { statement ->
            when {
                statement is IrVariable -> {
                    RumInternalStateVariable(rumClass, stateVariableIndex, statement).register(statement)
                }

                statement.startOffset < rumClass.boundary.startOffset -> {
                    rumClass.initializerStatements += statement
                }

                else -> {
                    rumClass.renderingStatements += statement
                }
            }
        }
    }

    fun RumStateVariable.register(declaration: IrDeclaration) {

        if (declaration.startOffset >= rumClass.boundary.startOffset) {
            RUI_IR_RENDERING_VARIABLE.report(rumClass, declaration)
            return
        }

        if (originalName in names) {
            // variable shadowing is a bad practice anyway, no big loss to forbid it
            RUI_IR_STATE_VARIABLE_SHADOW.report(rumClass, declaration)
            return
        }

        stateVariableIndex++
        names += originalName
        rumClass.stateVariables += this

        val maskNumber = this.index / RUI_STATE_VARIABLE_LIMIT

        if (rumClass.dirtyMasks.size <= maskNumber) {
            rumClass.dirtyMasks += RumDirtyMask(rumClass, maskNumber)
        }

    }

}
