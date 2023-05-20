package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirEntryPoint2Ir
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass

class AirEntryPoint(
    override val rumElement: RumClass,
    val airClass: AirClass
) : AirElement {

    fun toIr(context: RuiPluginContext) = AirEntryPoint2Ir(context, this).toIr()

}