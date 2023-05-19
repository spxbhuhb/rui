package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumWhen
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirWhen(
    override val rumElement: RumWhen,
    override val externalPatch: IrSimpleFunction,
    override val builder: IrSimpleFunction,
    val select: IrSimpleFunction,
    val branches: List<IrSimpleFunction>
) : SirRenderingStatement