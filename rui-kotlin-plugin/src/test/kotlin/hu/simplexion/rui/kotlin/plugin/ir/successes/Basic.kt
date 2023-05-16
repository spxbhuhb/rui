/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.successes

import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.testing.RuiTestFunction
import hu.simplexion.rui.runtime.testing.T1

@Rui
@RuiTestFunction
fun Basic(i: Int) {
    val i2 = 12
    T1(0)
//    T1(i)
//    T1(i2)
//    T1(i + i2)
//    if (i == 1) {
//        T1(i2)
//    }
//    when {
//        i == 1 -> T1(i2 + 1)
//        i == 2 -> T1(i2 + 2)
//    }
//    for (fi in i..i2) {
//        T1(fi + i2)
//    }
}