package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirExternalPatch
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall

context(ClassBoundIrBuilder)
object Call {

    fun toAir(call: RumCall): AirBuilder = with(call) {

        val externalPatch = AirExternalPatch(
            this,
            externalPatch(irCall.startOffset)
        )

        val builder = AirBuilder(
            this,
            builder(irCall.startOffset),
            externalPatch,
            emptyList()
        )

        airClass.functions += externalPatch
        airClass.functions += builder

        return builder
    }

}
