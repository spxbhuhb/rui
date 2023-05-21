package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumRenderingStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

interface AirFunction : AirElement {

    override val rumElement: RumRenderingStatement
    val irFunction: IrSimpleFunction

    fun toIr(parent: ClassBoundIrBuilder)

}