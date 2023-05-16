package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirDirtyMask(
    property: IrProperty,
    invalidate: IrSimpleFunction
) : AirElement {
}