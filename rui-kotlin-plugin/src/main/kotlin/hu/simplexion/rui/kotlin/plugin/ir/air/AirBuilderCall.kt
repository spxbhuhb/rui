package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirBuilderCall2Ir
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirBuilderCall(
    override val rumElement: RumCall,
    override val irFunction: IrSimpleFunction,
    override val externalPatch: AirFunction,
    override val subBuilders: List<AirBuilderBlock>
) : AirBuilder {

    override fun toIr(parent: ClassBoundIrBuilder) = AirBuilderCall2Ir(parent, this).toIr()

}