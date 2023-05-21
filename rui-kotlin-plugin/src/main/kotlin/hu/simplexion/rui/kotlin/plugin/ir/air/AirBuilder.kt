package hu.simplexion.rui.kotlin.plugin.ir.air

interface AirBuilder : AirFunction {

    val externalPatch: AirFunction
    val subBuilders: List<AirBuilder>

}