package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilderCall
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.StateAccessTransform.Companion.transformStateAccess
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

class AirBuilderCall2Ir(
    parent: ClassBoundIrBuilder,
    val call: AirBuilderCall
) : ClassBoundIrBuilder(parent) {

    fun toIr() {
        val rumCall = call.rumElement
        val symbolMap = rumCall.target.symbolMap
        val irFunction = call.irFunction

        val startScope = irFunction.valueParameters[RUI_BUILDER_ARGUMENT_INDEX_START_SCOPE]

        irFunction.body = DeclarationIrBuilder(irContext, irFunction.symbol).irBlockBody {

            val receiver = irFunction.dispatchReceiverParameter!!

            IrConstructorCallImpl(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                symbolMap.defaultType,
                symbolMap.primaryConstructor.symbol,
                typeArgumentsCount = 1, // bridge type
                constructorTypeArgumentsCount = 0,
                rumCall.valueArguments.size + RUI_FRAGMENT_ARGUMENT_COUNT // +3 = adapter + scope + external patch
            ).also { constructorCall ->

                constructorCall.putTypeArgument(RUI_FRAGMENT_TYPE_INDEX_BRIDGE, classBoundBridgeType.defaultType)

                constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER, irGetValue(airClass.adapter, irGet(receiver)))
                constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_SCOPE, irGetValue(airClass.scope, irGet(receiver)))
                constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_EXTERNAL_PATCH, irExternalPatchReference(startScope))

                rumCall.valueArguments.forEachIndexed { index, ruiExpression ->
                    constructorCall.putValueArgument(
                        index + RUI_FRAGMENT_ARGUMENT_COUNT,
                        transformStateAccess(ruiExpression, startScope.symbol)
                    )
                }
            }
        }
    }

    fun irExternalPatchReference(scope: IrValueParameter): IrExpression =
        IrFunctionReferenceImpl.fromSymbolOwner(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            classBoundExternalPatchType,
            call.externalPatch,
            typeArgumentsCount = 0,
            reflectionTarget = call.externalPatch
        ).also {
            it.dispatchReceiver = irGet(scope)
        }
}
