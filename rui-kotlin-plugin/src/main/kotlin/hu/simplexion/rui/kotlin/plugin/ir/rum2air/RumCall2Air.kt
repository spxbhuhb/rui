package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilderCall
import hu.simplexion.rui.kotlin.plugin.ir.air.AirExternalPatchCall
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall

class RumCall2Air(
    parent: ClassBoundIrBuilder,
    val rumCall: RumCall
) : ClassBoundIrBuilder(parent) {

    fun toAir(): AirBuilderCall = with(rumCall) {

        val externalPatch = AirExternalPatchCall(
            rumCall,
            externalPatch(irCall.startOffset)
        )
        airClass.functions += externalPatch

        val builder = AirBuilderCall(
            rumCall,
            builder(irCall.startOffset),
            externalPatch,
            emptyList()
        )
        airClass.functions += builder

        return builder
    }

}
