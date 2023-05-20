package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirCall

class AirCall2Ir(
    parent: ClassBoundIrBuilder,
    val call: AirCall
) : ClassBoundIrBuilder(parent) {

    fun toIr() {
        TODO()
    }

}
