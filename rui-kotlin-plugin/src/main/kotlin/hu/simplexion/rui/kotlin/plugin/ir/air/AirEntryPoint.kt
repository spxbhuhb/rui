package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirEntryPoint(
    val originalFunction: IrSimpleFunction,
    val externalPatch: IrSimpleFunction
) : AirElement {
}