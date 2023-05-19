package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumDirtyMask
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirDirtyMask(
    override val rumElement: RumDirtyMask,
    property: IrProperty,
    invalidate: IrSimpleFunction
) : SirElement