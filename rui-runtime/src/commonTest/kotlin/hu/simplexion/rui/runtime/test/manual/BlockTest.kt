/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime.test.manual

import hu.simplexion.rui.runtime.RuiAdapter
import hu.simplexion.rui.runtime.RuiBlock
import hu.simplexion.rui.runtime.RuiFragment
import hu.simplexion.rui.runtime.RuiGeneratedFragment
import hu.simplexion.rui.runtime.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockTest {

    @Test
    fun test() {
        val adapter = RuiTestAdapter()
        val root = RuiTestBridge(1)

        Block(adapter).apply {
            ruiCreate()
            ruiMount(root)
        }

        assertEquals(testResult, adapter.trace.joinToString("\n"))
    }

    val testResult = """
        [ RuiT1                          ]  init                  |  
        [ RuiT1                          ]  create                |  p0: 1
        [ RuiT0                          ]  create                |  
        [ RuiT1                          ]  mount                 |  bridge: 1
        [ RuiT0                          ]  mount                 |  bridge: 1
    """.trimIndent()

}


@Suppress("unused")
class Block(
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
            it.ruiPatch()
        }
    }

    override fun ruiPatch() {
        ruiFragment.ruiExternalPatch(ruiFragment) // TODO-- optimize out ruiExternalPatch for RuiBlock, it's an empty call
        ruiFragment.ruiPatch()
        ruiDirty0 = 0
    }

    init {
        ruiFragment = RuiBlock(
            ruiAdapter,
            RuiT1(ruiAdapter, this, ::ruiEp1, v0),
            RuiT0(ruiAdapter, this) { }
        )
    }

}