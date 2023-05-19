package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumHigherOrderCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirHigherOrderCall(
    override val rumElement: RumHigherOrderCall,
    override val externalPatch: IrSimpleFunction,
    override val builder: IrSimpleFunction,
    val higherOrderArguments: List<SirHigherOrderArgument>
) : SirRenderingStatement