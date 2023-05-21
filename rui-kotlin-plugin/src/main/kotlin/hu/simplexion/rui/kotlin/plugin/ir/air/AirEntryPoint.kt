package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirEntryPoint2Ir
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumEntryPoint

class AirEntryPoint(
    override val rumElement: RumEntryPoint,
    val airClass: AirClass
) : AirElement {

    val rumEntryPoint
        get() = rumElement

    fun toIr(context: RuiPluginContext) = AirEntryPoint2Ir(context, this).toIr()

}