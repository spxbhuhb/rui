/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder

abstract class RumRenderingStatement(
    val rumClass: RumClass,
    val index: Int,
) : RumElement {

    abstract val name: String

    abstract fun symbolMap(irBuilder: ClassBoundIrBuilder): RuiClassSymbols

    abstract fun toAir(parent: ClassBoundIrBuilder): AirBuilder

}