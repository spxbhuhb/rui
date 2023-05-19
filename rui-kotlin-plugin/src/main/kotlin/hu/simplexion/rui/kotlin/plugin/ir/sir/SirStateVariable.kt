package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumStateVariable
import org.jetbrains.kotlin.ir.declarations.IrProperty

class SirStateVariable(
    override val rumElement: RumStateVariable,
    val property: IrProperty,
) : SirElement {
}