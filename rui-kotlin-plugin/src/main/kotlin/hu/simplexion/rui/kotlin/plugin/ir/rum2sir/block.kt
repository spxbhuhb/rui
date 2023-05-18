package hu.simplexion.rui.kotlin.plugin.ir.rum2sir

import hu.simplexion.rui.kotlin.plugin.ir.RUI_BLOCK_ARGUMENT_COUNT
import hu.simplexion.rui.kotlin.plugin.ir.RUI_FQN_BLOCK_CLASS
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import hu.simplexion.rui.kotlin.plugin.ir.sir.SirBlock
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

context(ClassBoundIrBuilder)
fun RumBlock.toSir(): SirBlock {

    val symbolMap = context.ruiSymbolMap.getSymbolMap(RUI_FQN_BLOCK_CLASS)

    return SirBlock(
        newBlockInstance(symbolMap),
        statements = emptyList()
    )
}

context(ClassBoundIrBuilder)
fun newBlockInstance(symbolMap: RuiClassSymbols): IrConstructorCall =
    IrConstructorCallImpl(
        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
        symbolMap.defaultType,
        symbolMap.primaryConstructor.symbol,
        0, 0,
        RUI_BLOCK_ARGUMENT_COUNT // adapter, array of fragments
    )