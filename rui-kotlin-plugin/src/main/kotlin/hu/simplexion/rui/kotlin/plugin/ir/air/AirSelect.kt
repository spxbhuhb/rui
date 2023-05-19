package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumWhen
import org.jetbrains.kotlin.ir.declarations.IrFunction

class AirSelect(
    override val rumElement: RumWhen,
    override val irFunction: IrFunction
) : AirFunction