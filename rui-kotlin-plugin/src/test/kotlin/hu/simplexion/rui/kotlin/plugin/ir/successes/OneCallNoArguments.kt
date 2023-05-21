/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.successes

import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.testing.RuiTestFunction
import hu.simplexion.rui.runtime.testing.T0

@Rui
@RuiTestFunction
fun OneCallNoArguments() {
    T0()
}