/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import hu.simplexion.rui.kotlin.plugin.ir.RUI_STATE_VARIABLE_LIMIT
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumExternalStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumInternalStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumStateVariable
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.Name

@Deprecated("move to IR-RUM-AIR-IR")
class RuiStateVariableBuilder private constructor(
    ruiClassBuilder: RuiClassBuilder,
    val ruiStateVariable: RumStateVariable,
    name: Name,
    type: IrType
) : RuiPropertyBuilder(ruiClassBuilder, name, type) {

    companion object {
        fun builderFor(ruiClassBuilder: RuiClassBuilder, ruiStateVariable: RumExternalStateVariable) =
            RuiStateVariableBuilder(
                ruiClassBuilder,
                ruiStateVariable,
                ruiStateVariable.name,
                ruiStateVariable.irValueParameter.type
            ).apply {
                initExternal(ruiStateVariable)
            }


        fun builderFor(ruiClassBuilder: RuiClassBuilder, ruiStateVariable: RumInternalStateVariable) =
            RuiStateVariableBuilder(
                ruiClassBuilder,
                ruiStateVariable,
                ruiStateVariable.name,
                ruiStateVariable.irVariable.type
            ).apply {
                initInternal(ruiStateVariable)
            }
    }

    fun initExternal(ruiStateVariable: RumExternalStateVariable) {

        val constructorParameter = ruiClassBuilder.constructor.addValueParameter {
            name = this@RuiStateVariableBuilder.name
            type = ruiStateVariable.irValueParameter.type
            varargElementType = ruiStateVariable.irValueParameter.varargElementType
        }

        irField.initializer = irFactory
            .createExpressionBody(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                IrGetValueImpl(
                    SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, constructorParameter.symbol, IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
                )
            )

    }

    fun initInternal(ruiStateVariable: RumInternalStateVariable) {

        ruiStateVariable.irVariable.initializer?.let { initializer ->
            irField.initializer = irFactory.createExpressionBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, initializer)
        }

    }

//    fun irIsDirty(receiver: IrExpression): IrExpression {
//        val variableIndex = ruiStateVariable.index
//        val maskIndex = variableIndex / RUI_STATE_VARIABLE_LIMIT
//        val bitIndex = variableIndex % RUI_STATE_VARIABLE_LIMIT
//
//        val mask = ruiClassBuilder.rumClass.dirtyMasks[maskIndex]
//
//        return irNotEqual(
//            irAnd(mask.builder.propertyBuilder.irGetValue(receiver), irConst(1L shl bitIndex)),
//            irConst(0L)
//        )
//    }

}