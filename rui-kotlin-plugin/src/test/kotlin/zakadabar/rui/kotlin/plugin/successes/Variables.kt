/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package zakadabar.rui.kotlin.plugin.successes

import zakadabar.rui.runtime.Rui
import zakadabar.rui.runtime.testing.T1

@Rui
fun Variables(i: Int, s: String) {
    val i2 = 12

    T1(0)
    T1(i)
    T1(i2)
}