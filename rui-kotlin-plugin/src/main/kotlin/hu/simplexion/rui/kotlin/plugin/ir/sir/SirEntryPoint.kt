package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumEntryPoint
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirEntryPoint(
    override val rumElement: RumEntryPoint,
    val originalFunction: IrSimpleFunction,
    val externalPatch: IrSimpleFunction
) : SirElement