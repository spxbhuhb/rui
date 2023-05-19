package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class SirBlock(
    override val rumElement: RumBlock,
    override val externalPatch: IrSimpleFunction,
    override val builder: IrSimpleFunction,
) : SirRenderingStatement