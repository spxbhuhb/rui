package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumRenderingStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction

interface AirFunction : AirElement {
    override val rumElement: RumRenderingStatement
    val irFunction: IrFunction
}