package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RUI_FQN_BLOCK_CLASS
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilderBlock
import hu.simplexion.rui.kotlin.plugin.ir.air.AirExternalPatchBlock
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock

class RumBlock2Air(
    parent: ClassBoundIrBuilder,
    val rumBlock: RumBlock
) : ClassBoundIrBuilder(parent) {

    fun toAir(): AirBuilderBlock = with(rumBlock) {

        val symbolMap = context.ruiSymbolMap.getSymbolMap(RUI_FQN_BLOCK_CLASS)

        val externalPatch = AirExternalPatchBlock(
            rumBlock,
            externalPatch(irBlock.startOffset),
            symbolMap
        )
        airClass.functions += externalPatch

        val builder = AirBuilderBlock(
            rumBlock,
            symbolMap,
            builder(irBlock.startOffset),
            externalPatch.irFunction.symbol,
            statements.map { it.toAir(this@RumBlock2Air) }
        )
        airClass.functions += builder

        return builder
    }

}