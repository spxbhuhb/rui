/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime.test.manual

import hu.simplexion.rui.runtime.RuiAdapter
import hu.simplexion.rui.runtime.RuiFragment
import hu.simplexion.rui.runtime.RuiGeneratedFragment
import hu.simplexion.rui.runtime.RuiImplicit0
import hu.simplexion.rui.runtime.testing.RuiH1
import hu.simplexion.rui.runtime.testing.RuiT1
import hu.simplexion.rui.runtime.testing.TestNode

@Suppress("unused")
class TestHigherOrder(
    override val ruiAdapter: RuiAdapter<TestNode>
) : RuiGeneratedFragment<TestNode> {

    override val ruiParent: RuiFragment<TestNode>? = null
    override val ruiExternalPatch: (it: RuiFragment<TestNode>) -> Unit = { }

    override val fragment: RuiFragment<TestNode>

    var v0 = 1

    var ruiDirty0 = 0

    @Suppress("unused")
    fun ruiInvalidate0(mask: Int) {
        ruiDirty0 = ruiDirty0 or mask
    }

    @Suppress("UNUSED_PARAMETER")
    fun ruiEp0(it: RuiFragment<TestNode>) {

    }

    fun ruiEp1(it: RuiFragment<TestNode>) {
        it as RuiT1
        if (ruiDirty0 and 1 != 0) {
            it.p0 = v0
            it.ruiInvalidate0(1)
            it.ruiPatch()
        }
    }

    fun ruiBuilder0(ruiAdapter: RuiAdapter<TestNode>) =
        RuiImplicit0(ruiAdapter, this, ::ruiEp0).also {
            it.fragment = RuiT1(ruiAdapter, it, ::ruiEp1, v0)
        }

    init {
        fragment = RuiH1(ruiAdapter, this, ::ruiEp0, ::ruiBuilder0)
    }
}