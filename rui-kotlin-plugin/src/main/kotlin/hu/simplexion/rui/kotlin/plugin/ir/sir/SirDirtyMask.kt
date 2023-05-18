package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirDirtyMask(
    property: IrProperty,
    invalidate: IrSimpleFunction
) : SirElement {
}