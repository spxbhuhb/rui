package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass

class AirEntryPoint(
    override val rumElement: RumClass,
    val airClass: AirClass
) : AirElement