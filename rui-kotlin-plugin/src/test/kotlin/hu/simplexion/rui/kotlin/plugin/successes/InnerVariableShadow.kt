/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.successes

import hu.simplexion.rui.runtime.Rui

@Suppress("unused", "TestFunctionName")
@Rui
fun InnerVariableShadow(v: Int) {
    if (v == 1) {
        @Suppress("NAME_SHADOWING")
        val v = "s" // I would say this is a lesser kind of perversion...
        println(v)
    }
}