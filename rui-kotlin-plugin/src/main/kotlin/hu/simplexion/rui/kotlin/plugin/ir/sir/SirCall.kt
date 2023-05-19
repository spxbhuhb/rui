package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirCall(
    override val rumElement: RumCall,
    override val externalPatch: IrSimpleFunction,
    override val builder: IrSimpleFunction
) : SirRenderingStatement {
}