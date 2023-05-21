package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumWhen
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirBuilderWhen(
    override val rumElement: RumWhen,
    override val irFunction: IrSimpleFunction,
    override val externalPatch: AirFunction,
    override val subBuilders: List<AirBuilderBlock>
) : AirBuilder {

    override fun toIr(parent: ClassBoundIrBuilder) = TODO()

}