/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.builders

import hu.simplexion.rui.kotlin.plugin.*
import hu.simplexion.rui.kotlin.plugin.model.RuiClass
import hu.simplexion.rui.kotlin.plugin.model.RuiEntryPoint
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.name.Name

class RuiEntryPointBuilder(
    override val ruiClassBuilder: RuiClassBuilder,
    val ruiEntryPoint: RuiEntryPoint
) : RuiBuilder {

    val function
        get() = ruiEntryPoint.irFunction

    fun build() {
        ruiEntryPoint.irFunction.body = IrBlockBodyImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).apply {

            val instance = IrConstructorCallImpl(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                ruiClass.irClass.defaultType,
                ruiClass.builder.constructor.symbol,
                0, 0,
                RUI_FRAGMENT_ARGUMENT_COUNT
            ).also { call ->
                call.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER, irGetAdapter(function))
                call.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_SCOPE, irNull())
                call.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_EXTERNAL_PATCH, irExternalPatch(ruiClass, function.symbol))
            }

            val root = irTemporary(instance, "root").also { it.parent = ruiEntryPoint.irFunction }

            statements += root

            statements += irCall(
                ruiClass.builder.create.symbol,
                dispatchReceiver = irGet(root)
            )

            statements += irCall(
                ruiClass.builder.mount.symbol,
                dispatchReceiver = irGet(root),
                args = arrayOf(
                    irCall(
                        ruiContext.ruiAdapterClass.getPropertyGetter(RUI_ROOT_BRIDGE)!!.owner.symbol,
                        dispatchReceiver = irGetAdapter(function)
                    )
                )
            )
        }

        RuiDumpPoint.KotlinLike.dump(ruiContext) {
            ruiContext.output("KOTLIN LIKE", irClass.dumpKotlinLike(), irClass)
        }
    }


    private fun irGetAdapter(function: IrSimpleFunction): IrExpression =
        IrGetValueImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            function.valueParameters.first().symbol
        )

    fun irExternalPatch(ruiClass: RuiClass, parent: IrSimpleFunctionSymbol): IrExpression {
        val function = irFactory.buildFun {
            name = Name.special("<anonymous>")
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            returnType = irBuiltIns.unitType
        }.also { function ->

            function.parent = parent.owner
            function.visibility = DescriptorVisibilities.LOCAL

            function.addValueParameter {
                name = Name.identifier("it")
                type = ruiContext.ruiFragmentType
            }

            function.body = DeclarationIrBuilder(ruiContext.irContext, function.symbol).irBlockBody { }
        }

        return IrFunctionExpressionImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            ruiClass.builder.classBoundExternalPatchType,
            function,
            IrStatementOrigin.LAMBDA
        )
    }

}