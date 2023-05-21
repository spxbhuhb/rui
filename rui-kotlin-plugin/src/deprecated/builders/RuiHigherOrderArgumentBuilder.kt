/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumHigherOrderArgument
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.Name

@Deprecated("move to IR-RUM-AIR-IR")
class RuiHigherOrderArgumentBuilder(
    override val ruiClassBuilder: RuiClassBuilder,
    val ruiHigherOrderArgument: RumHigherOrderArgument
) : RuiBuilderWithSymbolMap {

    override lateinit var symbolMap: RuiClassSymbols

    lateinit var implicitComponentBuilderFunction: IrSimpleFunction
    lateinit var implicitComponentExternalPatchBuilder: RuiExternalPatchBuilder

    fun buildDeclarations() {
        buildImplicitComponentExternalPatch()
        buildImplicitComponentBuilderFunction()
        //buildParameterComponentBuilder()
    }

    fun buildImplicitComponentExternalPatch() {
        with(ruiHigherOrderArgument) {
            RuiExternalPatchBuilder(
                ruiClassBuilder,
                value.startOffset,
                emptyList(),
                0L, // FIXME dependency mask of implicit components
                symbolMap
            ).also {
                it.buildExternalPatch()
                implicitComponentExternalPatchBuilder = it
            }
        }
    }

    /**
     * ```kotlin
     * fun ruiBuilderNNN(ruiAdapter: RuiAdapter<BT>) {
     *     return RuiImplicit0(ruiAdapter, this, ::ruiEpNNN).also {
     *         it.ruiFragment = RuiT1(ruiAdapter, it, ::ruiEpMMM, i)
     *     }
     * }
     * ```
     */
    fun buildImplicitComponentBuilderFunction() {
        irFactory.buildFun {
            name = Name.identifier("$RUI_BUILDER${ruiHigherOrderArgument.irExpression.startOffset}")
            returnType = ruiContext.ruiFragmentType
            modality = Modality.OPEN
        }.also { function ->

            function.parent = irClass

            function.addDispatchReceiver {
                type = irClass.typeWith(irClass.typeParameters.first().defaultType)
            }

            function.addValueParameter {
                name = Name.identifier("ruiAdapter")
                type = classBoundAdapterType
            }

            function.body = irImplicitComponentBuilderBody(function)

            implicitComponentBuilderFunction = function
            irClass.declarations += function
        }
    }

    /**
     * ```kotlin
     *     return RuiImplicit0(ruiAdapter, this, ::ruiEpNNN).also {
     *         it.ruiFragment = RuiT1(ruiAdapter, it, ::ruiEpMMM, i)
     *     }
     * ```
     */
    private fun irImplicitComponentBuilderBody(function: IrSimpleFunction): IrBody =
        DeclarationIrBuilder(irContext, function.symbol).irBlockBody {

            val implicitSymbolMap = when (ruiHigherOrderArgument.valueParameters.size) {
                0 -> ruiContext.implicit0SymbolMap
                else -> TODO("")
            }

            // RuiImplicit0(ruiAdapter, this, ::ruiEpNNN)

            val call = IrConstructorCallImpl(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                implicitSymbolMap.defaultType,
                implicitSymbolMap.primaryConstructor.symbol,
                typeArgumentsCount = 1, // bridge type
                constructorTypeArgumentsCount = 0,
                ruiHigherOrderArgument.valueParameters.size + RUI_FRAGMENT_ARGUMENT_COUNT // +3 = adapter + scope + external patch
            ).also { constructorCall ->

                constructorCall.putTypeArgument(RUI_FRAGMENT_TYPE_INDEX_BRIDGE, classBoundBridgeType.defaultType)

                constructorCall.putValueArgument(
                    RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER,
                    ruiClassBuilder.adapterPropertyBuilder.irGetValue()
                )
                constructorCall.putValueArgument(
                    RUI_FRAGMENT_ARGUMENT_INDEX_SCOPE,
                    ruiClassBuilder.scopePropertyBuilder.irGetValue()
                )
//                constructorCall.putValueArgument(
//                    RUI_FRAGMENT_ARGUMENT_INDEX_EXTERNAL_PATCH,
//                    implicitComponentExternalPatchBuilder.irExternalPatchReference()
//                )
            }

            val instance = irTemporary(call)



            +irReturn(irGet(instance))
        }

}