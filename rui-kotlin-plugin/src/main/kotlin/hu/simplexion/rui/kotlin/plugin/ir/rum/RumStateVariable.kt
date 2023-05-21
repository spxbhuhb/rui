/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirStateVariable
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.name.Name

interface RumStateVariable : RumElement {

    val rumClass: RumClass
    val index: Int
    val originalName: String
    val name: Name

    fun matches(symbol: IrSymbol): Boolean

    fun toAir(parent: ClassBoundIrBuilder): AirStateVariable

}