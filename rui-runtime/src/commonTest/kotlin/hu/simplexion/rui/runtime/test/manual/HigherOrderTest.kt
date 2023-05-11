/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime.test.manual

import hu.simplexion.rui.runtime.*
import hu.simplexion.rui.runtime.testing.*
import kotlin.test.assertEquals

class HigherOrderTest {

    //@Test
    fun test() {
        val adapter = RuiTestAdapter()
        val root = RuiTestBridge(1)

        HigherOrder(adapter).apply {
            ruiCreate()
            ruiMount(root)
            i = 13
            ruiInvalidate0(1)
            ruiPatch(1)
        }

        assertEquals(testResult, adapter.trace.joinToString("\n"))
    }

    val testResult = """
[ RuiT1                          ]  init                  |  
[ RuiH1                          ]  init                  |  
[ RuiH1                          ]  create                |  
[ RuiT1                          ]  create                |  p0: 12
[ RuiH1                          ]  mount                 |  bridge: 1
[ RuiT1                          ]  mount                 |  bridge: 1
[ RuiT1                          ]  mount                 |  bridge: 1
[ HigherOrder                    ]  ruiEp0                |  ruiDirty0: 1 i: 13
[ RuiH1                          ]  patch                 |  
[ HigherOrder                    ]  ruiEp1                |  ruiDirty0: 1 i: 13
[ RuiT1                          ]  invalidate            |  mask: 1 ruiDirty0: 0
[ RuiT1                          ]  patch                 |  ruiDirty0: 1 p0: 13
[ RuiT1                          ]  patch                 |  ruiDirty0: 0 p0: 13
    """.trimIndent()

}

/**
 * ```kotlin
 * fun higherOrder() {
 *     var i = 12
 *     H1 {
 *         T1(i)
 *     }
 * }
 * ```
 */
@Suppress("unused")
class HigherOrder(
    override val ruiAdapter: RuiAdapter<TestNode>
) : RuiGeneratedFragment<TestNode> {

    override val ruiScope: RuiFragment<TestNode>? = null
    override val ruiExternalPatch: RuiExternalPathType<TestNode> = { _, scopeMask -> scopeMask }

    override val ruiFragment: RuiFragment<TestNode>

    var i = 12

    var ruiDirty0 = 0

    @Suppress("unused")
    fun ruiInvalidate0(mask: Int) {
        ruiDirty0 = ruiDirty0 or mask
    }

    @Suppress("UNUSED_PARAMETER")
    fun ruiEp0(it: RuiFragment<TestNode>, scopeMask: Long): Long {
        ruiAdapter.trace("HigherOrder", "ruiEp0", "ruiDirty0:", ruiDirty0, "i:", i)
        return 0L
    }

    fun ruiEp1(it: RuiFragment<TestNode>, scopeMask: Long): Long {
        ruiAdapter.trace("HigherOrder", "ruiEp1", "ruiDirty0:", ruiDirty0, "i:", i)
        if (scopeMask and 1 != 0L) return 0L

        it as RuiT1
        if (ruiDirty0 and 1 != 0) {
            it.p0 = i
            it.ruiInvalidate0(1)
        }

        return 0L
    }

    override fun ruiPatch(scopeMask: Long) {
        val extendedScopeMask = ruiFragment.ruiExternalPatch(ruiFragment, scopeMask)
        if (extendedScopeMask != 0L) ruiFragment.ruiPatch(extendedScopeMask)
        ruiDirty0 = 0
    }

    fun ruiBuilder0(ruiAdapter: RuiAdapter<TestNode>) =
        RuiImplicit0(ruiAdapter, this, ::ruiEp0).also {
            it.ruiFragment = RuiT1(ruiAdapter, it, ::ruiEp1, i)
        }

    init {
        ruiFragment = RuiH1(ruiAdapter, this, ::ruiEp0, ::ruiBuilder0)
    }
}