/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.successes

import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.testing.RuiTestFunction
import hu.simplexion.rui.runtime.testing.T1

@Rui
@RuiTestFunction
fun Variables(i: Int, s: String) {
    val i2 = 12

    T1(0)
    T1(i)
    T1(i2)
}