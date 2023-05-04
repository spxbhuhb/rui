/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.errors

import hu.simplexion.rui.runtime.Rui

@Suppress("unused", "TestFunctionName")
@Rui
fun StateVariableShadow(v: Int) {
    @Suppress("UNUSED_VARIABLE", "NAME_SHADOWING")
    var v = v.toString() // I would say this is a greater kind of perversion...
}