package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirBuilderBlock2Ir
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirBuilderBlock(
    override val rumElement: RumBlock,
    override val irFunction: IrSimpleFunction,
    override val externalPatch: AirFunction,
    override val subBuilders: List<AirBuilder>
) : AirBuilder {

    override fun toIr(parent: ClassBoundIrBuilder) = AirBuilderBlock2Ir(parent, this).toIr()

}