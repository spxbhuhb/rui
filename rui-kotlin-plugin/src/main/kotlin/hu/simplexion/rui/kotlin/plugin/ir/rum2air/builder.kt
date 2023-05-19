package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumRenderingStatement

context(ClassBoundIrBuilder)
fun RumRenderingStatement.toAir(): AirBuilder =
    when (this) {
        is RumBlock -> Block.toAir(this)
        is RumCall -> Call.toAir(this)
        else -> throw IllegalStateException()
    }


