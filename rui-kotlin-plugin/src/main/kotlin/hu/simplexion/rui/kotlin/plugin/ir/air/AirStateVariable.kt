package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumStateVariable
import org.jetbrains.kotlin.ir.declarations.IrProperty

class AirStateVariable(
    override val rumElement: RumStateVariable,
    override val irProperty: IrProperty
) : AirProperty