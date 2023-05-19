package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirExternalPatch
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock

context(ClassBoundIrBuilder)
object Block {

    fun toAir(block: RumBlock): AirBuilder = with(block) {

        val externalPatch = AirExternalPatch(
            this,
            externalPatch(irBlock.startOffset)
        )

        val builder = AirBuilder(
            this,
            builder(irBlock.startOffset),
            externalPatch,
            statements.map { it.toAir() }
        )

        airClass.functions += externalPatch
        airClass.functions += builder

        return builder

    }

}