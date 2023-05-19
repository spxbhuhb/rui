package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumRenderingStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirBuilder(
    override val rumElement: RumRenderingStatement,
    override val irFunction: IrSimpleFunction,
    val externalPatch: AirExternalPatch,
    val subBuilders: List<AirBuilder>
) : AirFunction