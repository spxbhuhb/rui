/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.errors

import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.testing.T0

@Rui
fun VariableInRendering() {
    T0()
    var i = 0 // error 0001 : variable declaration is not allowed in rendering
    T0()
}