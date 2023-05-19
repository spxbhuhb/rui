package hu.simplexion.rui.kotlin.plugin.ir.rum2sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import hu.simplexion.rui.kotlin.plugin.ir.sir.SirBlock
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder

context(ClassBoundIrBuilder)
fun RumBlock.toSir(): SirBlock {
    return SirBlock(
        this,
        externalPatch(this.irBlock.startOffset),
        builder(this.irBlock.startOffset),
    )
}