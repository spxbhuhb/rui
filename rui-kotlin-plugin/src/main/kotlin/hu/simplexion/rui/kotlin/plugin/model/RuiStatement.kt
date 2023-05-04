/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.model

import hu.simplexion.rui.kotlin.plugin.transform.builders.RuiFragmentBuilder

abstract class RuiStatement(
    val ruiClass: RuiClass,
    val index: Int,
) : RuiElement {

    abstract val name: String

    abstract val builder: RuiFragmentBuilder

}