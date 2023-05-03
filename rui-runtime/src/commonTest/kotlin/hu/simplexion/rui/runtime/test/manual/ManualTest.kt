/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime.test.manual

import hu.simplexion.rui.runtime.testing.RuiTestAdapter
import hu.simplexion.rui.runtime.testing.RuiTestBridge
import kotlin.test.Test

class ManualTest {

    @Test
    fun branchTest() {
        val root = RuiTestBridge(1)

        val c = Branch(RuiTestAdapter())
        c.ruiCreate()
        c.ruiMount(root)

        fun v(value: Int) {
            c.v0 = value
            c.ruiInvalidate0(1)
            c.ruiPatch()
        }

        v(1)
        v(2)
        v(3)
        v(1)
    }
}