package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumDirtyMask
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirDirtyMask(
    override val rumElement: RumDirtyMask,
    override val irProperty: IrProperty,
    val invalidate: IrSimpleFunction
) : AirProperty