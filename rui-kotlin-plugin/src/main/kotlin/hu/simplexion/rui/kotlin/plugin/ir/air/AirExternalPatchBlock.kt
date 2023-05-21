package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirExternalPatchBlock2Ir
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirExternalPatchBlock(
    override val rumElement: RumBlock,
    override val irFunction: IrSimpleFunction,
    override val symbolMap: RuiClassSymbols
) : AirFunction {

    override fun toIr(parent: ClassBoundIrBuilder) = AirExternalPatchBlock2Ir(parent, this).toIr()

}