package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET


context(ClassBoundIrBuilder, AirBuilder)
fun blockBuilder() {
    val symbolMap = RUI_FQN_BLOCK_CLASS.symbolMap

    val startScope = irFunction.valueParameters[RUI_BUILDER_ARGUMENT_INDEX_START_SCOPE]

    irFunction.body = DeclarationIrBuilder(irContext, irFunction.symbol).irBlockBody {

        IrConstructorCallImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            symbolMap.defaultType,
            symbolMap.primaryConstructor.symbol,
            0, 0,
            RUI_BLOCK_ARGUMENT_COUNT // adapter, array of fragments
        ).also { constructorCall ->

            constructorCall.putValueArgument(
                RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER,
                airClass.adapter.irGetValue(irGet(startScope))
            )

            constructorCall.putValueArgument(
                RUI_BLOCK_ARGUMENT_INDEX_FRAGMENTS,
                buildFragmentVarArg(startScope)
            )

        }
    }
}
