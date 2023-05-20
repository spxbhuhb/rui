package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RUI_FQN_BLOCK_CLASS
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBlock
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirExternalPatch
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock

class RumBlock2Air(
    parent: ClassBoundIrBuilder,
    val rumBlock: RumBlock
) : ClassBoundIrBuilder(parent) {

    fun toAir(): AirBuilder = with(rumBlock) {

        val externalPatch = AirExternalPatch(
            rumBlock,
            externalPatch(irBlock.startOffset)
        )

        return AirBlock(
            rumBlock,
            builder(irBlock.startOffset),
            context.ruiSymbolMap.getSymbolMap(RUI_FQN_BLOCK_CLASS),
            externalPatch,
            statements.map { it.toAir(this@RumBlock2Air) }
        )
    }

}