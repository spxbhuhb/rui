package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumElement
import org.jetbrains.kotlin.ir.declarations.IrProperty

interface AirProperty : AirElement {
    override val rumElement: RumElement
    val irProperty: IrProperty
}