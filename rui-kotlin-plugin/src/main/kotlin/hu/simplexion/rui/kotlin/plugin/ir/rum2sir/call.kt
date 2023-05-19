package hu.simplexion.rui.kotlin.plugin.ir.rum2sir

import hu.simplexion.rui.kotlin.plugin.ir.RUI_BLOCK_ARGUMENT_COUNT
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall
import hu.simplexion.rui.kotlin.plugin.ir.sir.SirCall
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

context(ClassBoundIrBuilder)
fun RumCall.toSir(): SirCall {

    return SirCall(
        this,
        externalPatch(irCall.startOffset),
        builder(irCall.startOffset)
    )
}

context(ClassBoundIrBuilder)
private fun newCallInstance(symbolMap: RuiClassSymbols): IrConstructorCall =
    IrConstructorCallImpl(
        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
        symbolMap.defaultType,
        symbolMap.primaryConstructor.symbol,
        0, 0,
        RUI_BLOCK_ARGUMENT_COUNT // adapter, array of fragments
    )