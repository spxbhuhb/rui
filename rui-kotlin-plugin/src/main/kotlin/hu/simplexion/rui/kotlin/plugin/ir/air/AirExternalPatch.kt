package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumRenderingStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction

class AirExternalPatch(
    override val rumElement: RumRenderingStatement,
    override val irFunction: IrFunction
) : AirFunction