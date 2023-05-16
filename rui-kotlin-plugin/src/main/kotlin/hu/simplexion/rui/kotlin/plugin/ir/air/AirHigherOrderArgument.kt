package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirHigherOrderArgument(
    val builder: IrSimpleFunction,
    val externalPatch: IrSimpleFunction,
    val rendering: AirRenderingStatement
) : AirElement {
}