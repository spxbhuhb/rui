/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiFragmentBuilder

abstract class RumRenderingStatement(
    val rumClass: RumClass,
    val index: Int,
) : RumElement {

    abstract val name: String

    abstract val builder: RuiFragmentBuilder

    abstract fun toAir(parent: ClassBoundIrBuilder): AirBuilder

}