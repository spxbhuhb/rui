package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.air.AirStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumExternalStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumInternalStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder

context(ClassBoundIrBuilder)
fun RumExternalStateVariable.toAir(): AirStateVariable {

    val property = airClass.constructor.addPropertyParameter(
        name,
        irValueParameter.type,
        inIsVar = true,
        inVarargElementType = irValueParameter.varargElementType
    )

    return AirStateVariable(
        property
    )
}

context(ClassBoundIrBuilder)
fun RumInternalStateVariable.toAir(): AirStateVariable {

    val property = addProperty(
        name,
        irVariable.type,
        inIsVar = true,
        inInitializer = irVariable.initializer
    )

    return AirStateVariable(
        property
    )
}