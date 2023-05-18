package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirEntryPoint(
    val originalFunction: IrSimpleFunction,
    val externalPatch: IrSimpleFunction
) : SirElement {
}