package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirBlock2Ir
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirBlock(
    rumBlock: RumBlock,
    irFunction: IrSimpleFunction,
    symbolMap: RuiClassSymbols,
    externalPatch: AirExternalPatch,
    subBuilders: List<AirBuilder>
) : AirBuilder(rumBlock, irFunction, symbolMap, externalPatch, subBuilders) {

    override fun toIr(parent: ClassBoundIrBuilder) = AirBlock2Ir(parent, this).toIr()

}