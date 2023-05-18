package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirHigherOrderArgument(
    val builder: IrSimpleFunction,
    val externalPatch: IrSimpleFunction,
    val rendering: SirRenderingStatement
) : SirElement {
}