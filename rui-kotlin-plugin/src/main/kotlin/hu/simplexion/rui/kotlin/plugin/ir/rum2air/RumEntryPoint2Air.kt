package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air.AirEntryPoint
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumEntryPoint

class RumEntryPoint2Air(
    context: RuiPluginContext,
    val entryPoint: RumEntryPoint
) : ClassBoundIrBuilder(context) {

    fun toAir(): AirEntryPoint {
        return AirEntryPoint(
            entryPoint,
            context.airClasses[entryPoint.rumClass.fqName]!!
        )
    }
}
