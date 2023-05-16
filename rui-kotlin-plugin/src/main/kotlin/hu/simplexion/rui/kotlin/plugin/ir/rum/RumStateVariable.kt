/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiStateVariableBuilder
import org.jetbrains.kotlin.name.Name

interface RumStateVariable : RumElement {

    val rumClass: RumClass
    val index: Int
    val originalName: String
    val name: Name

    val builder: RuiStateVariableBuilder

}