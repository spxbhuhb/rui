/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package zakadabar.rui.test

import hu.simplexion.rui.runtime.rui
import hu.simplexion.rui.runtime.testing.RuiTestAdapter
import hu.simplexion.rui.runtime.testing.T1

fun main() {
    rui(RuiTestAdapter()) {
        T1(12)
    }
}