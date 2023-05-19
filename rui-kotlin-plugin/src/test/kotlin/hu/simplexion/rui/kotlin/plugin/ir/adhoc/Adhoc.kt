/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.adhoc

import hu.simplexion.rui.runtime.Rui

//@Rui
//fun a(p0: Int = 0, @Rui stuff: () -> Unit) {
//    stuff()
//}
//
@Rui
fun a() {

}

//
//class HigherOrder(
//    override val ruiAdapter: RuiAdapter<TestNode>
//) : RuiGeneratedFragment<TestNode> {
//
//    override val ruiScope: RuiFragment<TestNode>? = null
//    override val ruiExternalPatch: (it: RuiFragment<TestNode>, scopeMask: Long) -> Long = { _, _ -> 0L }
//
//    override val ruiFragment: RuiFragment<TestNode>
//
//    var i = 12
//
//    var ruiDirty0 = 0L
//
//    @Suppress("unused")
//    fun ruiInvalidate0(mask: Long) {
//        ruiDirty0 = ruiDirty0 or mask
//    }
//
//    @Suppress("UNUSED_PARAMETER")
//    fun ruiEp0(it: RuiFragment<TestNode>, scopeMask: Long): Long {
//        ruiAdapter.trace("HigherOrder", "ruiEp0", "ruiDirty0:", ruiDirty0, "i:", i)
//        return scopeMask
//    }
//
//    fun ruiEp1(it: RuiFragment<TestNode>, scopeMask: Long): Long {
//        if (scopeMask and 1 == 0L) return 0L
//
//        it as RuiT1
//        if (ruiDirty0 and 1L != 0L) {
//            it.p0 = i
//            it.ruiInvalidate0(1)
//        }
//        return scopeMask
//    }
//
//    override fun ruiPatch(scopeMask: Long) {
//        val extendedScopeMask = ruiFragment.ruiExternalPatch(ruiFragment, scopeMask)
//        if (extendedScopeMask != 0L) ruiFragment.ruiPatch(extendedScopeMask)
//        ruiDirty0 = 0L
//    }
//
//    fun ruiBuilder0(ruiAdapter: RuiAdapter<TestNode>) =
//        RuiImplicit0(ruiAdapter, this, ::ruiEp0).also {
//            it.ruiFragment = RuiT1(ruiAdapter, it, ::ruiEp1, i)
//        }
//
//    init {
//        ruiFragment = RuiH1(ruiAdapter, this, ::ruiEp0, ::ruiBuilder0)
//    }
//}