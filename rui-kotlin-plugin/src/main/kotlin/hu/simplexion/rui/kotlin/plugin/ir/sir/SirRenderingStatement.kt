package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumRenderingStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

interface SirRenderingStatement : SirElement {
    override val rumElement: RumRenderingStatement
    val externalPatch: IrSimpleFunction
    val builder: IrSimpleFunction
}