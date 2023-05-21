package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumExternalStateVariable

class RumExternalStateVariable2Air(
    parent: ClassBoundIrBuilder,
    val stateVariable: RumExternalStateVariable
) : ClassBoundIrBuilder(parent) {

    fun toAir(): AirStateVariable = with(stateVariable) {

        val property = addPropertyWitConstructorParameter(
            name,
            irValueParameter.type,
            inIsVar = true,
            inVarargElementType = irValueParameter.varargElementType
        )

        return AirStateVariable(
            this,
            property
        )
    }


}