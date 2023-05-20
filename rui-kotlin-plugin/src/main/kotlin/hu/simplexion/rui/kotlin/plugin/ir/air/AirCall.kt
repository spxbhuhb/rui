package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirCall2Ir
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirCall(
    rumCall: RumCall,
    irFunction: IrSimpleFunction,
    symbolMap: RuiClassSymbols,
    externalPatch: AirExternalPatch,
    subBuilders: List<AirBuilder>
) : AirBuilder(rumCall, irFunction, symbolMap, externalPatch, subBuilders) {

    override fun toIr(parent: ClassBoundIrBuilder) = AirCall2Ir(parent, this).toIr()

}