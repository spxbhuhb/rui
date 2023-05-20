package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air.AirEntryPoint

class AirEntryPoint2Ir(
    context: RuiPluginContext,
    entryPoint: AirEntryPoint
) : ClassBoundIrBuilder(context, entryPoint.airClass) {

    fun toIr() {
        TODO()
    }

}