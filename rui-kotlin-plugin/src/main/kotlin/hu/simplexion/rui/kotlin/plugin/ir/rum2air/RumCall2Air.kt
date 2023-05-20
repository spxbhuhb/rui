package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirCall
import hu.simplexion.rui.kotlin.plugin.ir.air.AirExternalPatch
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall

class RumCall2Air(
    parent: ClassBoundIrBuilder,
    val rumCall: RumCall
) : ClassBoundIrBuilder(parent) {

    fun toAir(): AirBuilder = with(rumCall) {

        val externalPatch = AirExternalPatch(
            rumCall,
            externalPatch(irCall.startOffset)
        )

        return AirCall(
            rumCall,
            builder(irCall.startOffset),
            context.ruiSymbolMap.getSymbolMap(rumCall.target),
            externalPatch,
            emptyList()
        )
    }

}
