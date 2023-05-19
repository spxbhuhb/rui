package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumHigherOrderArgument
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirHigherOrderArgument(
    override val rumElement: RumHigherOrderArgument,
    val builder: IrSimpleFunction,
    val externalPatch: IrSimpleFunction,
    val rendering: SirRenderingStatement
) : SirElement {
}