package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air.AirStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumExternalStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumInternalStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumStateVariable

class StateVariable(
    override val context: RuiPluginContext
) : ClassBoundIrBuilder {

    fun toAir(rumStateVariable: RumStateVariable): AirStateVariable =
        with(rumStateVariable) {
            when (this) {
                is RumExternalStateVariable -> toAir()
                is RumInternalStateVariable -> toAir()
                else -> throw IllegalStateException()
            }
        }

    fun RumExternalStateVariable.toAir(): AirStateVariable {

        val property = addParameterProperty(
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

    fun RumInternalStateVariable.toAir(): AirStateVariable {

        val property = addProperty(
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