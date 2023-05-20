package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumInternalStateVariable

class RumInternalStateVariable2Air(
    parent: ClassBoundIrBuilder,
    val stateVariable: RumInternalStateVariable
) : ClassBoundIrBuilder(parent) {

    fun toAir(): AirStateVariable = with(stateVariable) {

        val property = addIrProperty(
            name,
            irVariable.type,
            inIsVar = true,
            inInitializer = irVariable.initializer
        )

        return AirStateVariable(
            this,
            property
        )
    }

}