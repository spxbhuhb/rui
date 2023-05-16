/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiStateVariableBuilder
import org.jetbrains.kotlin.name.Name

interface RuiStateVariable : RuiElement {

    val ruiClass: RuiClass
    val index: Int
    val originalName: String
    val name: Name

    val builder: RuiStateVariableBuilder

}