package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirExternalPatchCall2Ir
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirExternalPatchCall(
    override val rumElement: RumCall,
    override val irFunction: IrSimpleFunction,
    override val symbolMap: RuiClassSymbols
) : AirExternalPatch {

    override fun toIr(parent: ClassBoundIrBuilder) = AirExternalPatchCall2Ir(parent, this).toIr()

}