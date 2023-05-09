/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime.test.manual

import hu.simplexion.rui.runtime.*
import hu.simplexion.rui.runtime.testing.RuiT1
import hu.simplexion.rui.runtime.testing.RuiTestAdapter
import hu.simplexion.rui.runtime.testing.RuiTestBridge
import hu.simplexion.rui.runtime.testing.TestNode
import kotlin.test.Test
import kotlin.test.assertEquals

class BranchTest {

    @Test
    fun test() {
        val adapter = RuiTestAdapter()
        val root = RuiTestBridge(1)

        Branch(adapter).apply {
            ruiCreate()
            ruiMount(root)

            fun v(value: Int) {
                v0 = value
                ruiInvalidate0(1)
                ruiPatch()
            }

            v(1)
            v(2)
            v(3)
            v(1)
        }

        assertEquals(testResult, adapter.trace.joinToString("\n"))

    }

    val testResult = """
        [ RuiT1                          ]  init                  |  
        [ RuiT1                          ]  create                |  p0: 11
        [ RuiT1                          ]  mount                 |  bridge: 2
        [ RuiT1                          ]  patch                 |  ruiDirty0: 0 p0: 11
        [ RuiT1                          ]  unmount               |  bridge: 2
        [ RuiT1                          ]  dispose               |  
        [ RuiT1                          ]  init                  |  
        [ RuiT1                          ]  create                |  p0: 22
        [ RuiT1                          ]  mount                 |  bridge: 2
        [ RuiT1                          ]  unmount               |  bridge: 2
        [ RuiT1                          ]  dispose               |  
        [ RuiT1                          ]  init                  |  
        [ RuiT1                          ]  create                |  p0: 11
        [ RuiT1                          ]  mount                 |  bridge: 2
    """.trimIndent()

}

@Suppress("unused")
class Branch(
    override val ruiAdapter: RuiAdapter<TestNode>
) : RuiGeneratedFragment<TestNode> {

    override val ruiParent: RuiFragment<TestNode>? = null
    override val ruiExternalPatch: (it: RuiFragment<TestNode>) -> Unit = { }

    override val ruiFragment: RuiFragment<TestNode>

    var v0: Int = 1

    var ruiDirty0 = 0

    fun ruiInvalidate0(mask: Int) {
        ruiDirty0 = ruiDirty0 or mask
    }

    fun ruiEp0(it: RuiFragment<TestNode>) {
        it as RuiT1
        if (ruiDirty0 and 1 != 0) {
            it.p0 = v0 + 10
            ruiInvalidate0(1)
        }
    }

    fun ruiEp1(it: RuiFragment<TestNode>) {
        it as RuiT1
        if (ruiDirty0 and 1 != 0) {
            it.p0 = v0 + 20
            ruiInvalidate0(1)
        }
    }

    override fun ruiPatch() {
        ruiFragment.ruiExternalPatch(ruiFragment)
        ruiFragment.ruiPatch()
    }

    fun ruiBranch0(): RuiFragment<TestNode> = RuiT1(ruiAdapter, this, ::ruiEp0, v0 + 10)
    fun ruiBranch1(): RuiFragment<TestNode> = RuiT1(ruiAdapter, this, ::ruiEp1, v0 + 20)
    fun ruiBranch2(): RuiFragment<TestNode> = RuiPlaceholder(ruiAdapter)

    fun ruiSelect(): Int =
        when (v0) {
            1 -> 0 // index in RuiWhen.fragments
            2 -> 1
            else -> 2
        }

    init {
        ruiFragment = RuiWhen(
            ruiAdapter,
            ::ruiSelect,
            ::ruiBranch0,
            ::ruiBranch1,
            ::ruiBranch2
        )
    }
}