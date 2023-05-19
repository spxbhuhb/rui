package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET


context(ClassBoundIrBuilder)
fun AirBuilder.toRir() {
    when (this.rumElement) {
        is RumBlock -> blockBuilder()
    }
}

context(ClassBoundIrBuilder, AirBuilder)
internal fun buildFragmentVarArg(startScope: IrValueParameter): IrExpression {
    return IrVarargImpl(
        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
        irBuiltIns.arrayClass.typeWith(context.ruiFragmentType),
        context.ruiFragmentType,
    ).also { vararg ->
        subBuilders.forEach { sub ->

            val call = IrCallImpl(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, // TODO use proper offsets from the original source code
                context.ruiFragmentType,
                sub.irFunction.symbol,
                typeArgumentsCount = 0,
                valueArgumentsCount = 1
            ).apply {
                putValueArgument(0, irGet(startScope))
            }

            vararg.addElement(call)
        }
    }
}