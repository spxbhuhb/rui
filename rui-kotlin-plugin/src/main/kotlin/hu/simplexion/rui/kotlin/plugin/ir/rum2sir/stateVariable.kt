package hu.simplexion.rui.kotlin.plugin.ir.rum2sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumExternalStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumInternalStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.sir.SirStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder

context(ClassBoundIrBuilder)
fun RumExternalStateVariable.toSir(): SirStateVariable {

    val property = sirClass.constructor.addPropertyParameter(
        name,
        irValueParameter.type,
        inIsVar = true,
        inVarargElementType = irValueParameter.varargElementType
    )

    return SirStateVariable(
        property
    )
}

context(ClassBoundIrBuilder)
fun RumInternalStateVariable.toSir(): SirStateVariable {

    val property = addProperty(
        name,
        irVariable.type,
        inIsVar = true,
        inInitializer = irVariable.initializer
    )

    return SirStateVariable(
        property
    )
}