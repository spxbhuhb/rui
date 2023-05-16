/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import hu.simplexion.rui.kotlin.plugin.ir.RUI_INVALIDATE
import hu.simplexion.rui.kotlin.plugin.ir.RUI_MASK
import hu.simplexion.rui.kotlin.plugin.ir.model.RuiDirtyMask
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.Name

class RuiDirtyMaskBuilder(
    override val ruiClassBuilder: RuiClassBuilder,
    val ruiDirtyMask: RuiDirtyMask
) : RuiBuilder {

    val propertyBuilder = RuiPropertyBuilder(ruiClassBuilder, ruiDirtyMask.name, irBuiltIns.longType)
    val invalidate: IrSimpleFunctionSymbol

    init {
        initInitializer()
        invalidate = initInvalidate()
    }

    fun initInitializer() {
        propertyBuilder.irField.initializer = irFactory.createExpressionBody(irConst(0))
    }

    fun initInvalidate(): IrSimpleFunctionSymbol {
        return irFactory
            .buildFun {
                name = Name.identifier(RUI_INVALIDATE + ruiDirtyMask.index)
                returnType = irBuiltIns.unitType
                modality = Modality.OPEN
            }.also { function ->

                function.parent = irClass

                val receiver = function.addDispatchReceiver {
                    type = irClass.defaultType
                }

                val mask = function.addValueParameter {
                    name = Name.identifier(RUI_MASK)
                    type = irBuiltIns.longType
                }

                function.body = initInvalidateBody(function, receiver, mask)

                irClass.declarations += function
            }
            .symbol
    }

    private fun initInvalidateBody(function: IrSimpleFunction, receiver: IrValueParameter, mask: IrValueParameter): IrBody =
        DeclarationIrBuilder(irContext, function.symbol).irBlockBody {
            +propertyBuilder.irSetValue(
                irOr(
                    propertyBuilder.irGetValue(irGet(receiver)),
                    irGet(mask)
                ),
                receiver = irGet(receiver)
            )
        }

    fun irClear(receiver: IrValueParameter): IrStatement =
        propertyBuilder.irSetValue(
            irConst(0L),
            receiver = irGet(receiver)
        )

}