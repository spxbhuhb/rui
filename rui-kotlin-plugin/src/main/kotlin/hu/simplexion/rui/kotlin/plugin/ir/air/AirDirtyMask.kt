package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumDirtyMask
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty

class AirDirtyMask(
    override val rumElement: RumDirtyMask,
    override val irProperty: IrProperty,
    val invalidate: IrFunction
) : AirProperty