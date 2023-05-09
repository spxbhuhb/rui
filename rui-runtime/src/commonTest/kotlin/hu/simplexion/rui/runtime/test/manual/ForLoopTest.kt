/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime.test.manual

import hu.simplexion.rui.runtime.*
import hu.simplexion.rui.runtime.testing.*
import kotlin.test.assertEquals

class ForLoopTest {

    // not implemented yet @Test
    fun test() {
        val adapter = RuiTestAdapter()
        val root = RuiTestBridge(1)

        ForLoop(adapter).apply {
            ruiCreate()
            ruiMount(root)
        }

        assertEquals(testResult, adapter.trace.joinToString("\n"))
    }

    val testResult = """
        ...
    """.trimIndent()

}

@Suppress("unused")
class ForLoop(
    override val ruiAdapter: RuiAdapter<TestNode>
) : RuiGeneratedFragment<TestNode> {

    override val ruiParent: RuiFragment<TestNode>? = null
    override val ruiExternalPatch: (it: RuiFragment<TestNode>) -> Unit = { }

    override val ruiFragment: RuiFragment<TestNode>

    var v0 = 1

    var ruiDirty0 = 0

    fun ruiInvalidate0(mask: Int) {
        ruiDirty0 = ruiDirty0 or mask
    }

    fun ruiEp1(it: RuiFragment<TestNode>) {
        it as RuiT1
        if (ruiDirty0 and 1 != 0) {
            it.p0 = v0
            it.ruiInvalidate0(1)
        }
    }

    override fun ruiPatch() {
        ruiFragment.ruiExternalPatch(ruiFragment)
        ruiFragment.ruiPatch()
    }

    fun ruiIterator0() = IntRange(0, 10).iterator()

    fun ruiBuilder0() =
        RuiBlock(
            ruiAdapter,
            RuiT1(ruiAdapter, this, ::ruiEp1, v0),
            RuiT0(ruiAdapter, this) { }
        )

    init {
        ruiFragment = RuiLoop(
            ruiAdapter,
            ::ruiIterator0,
            ::ruiBuilder0
        )
    }
}