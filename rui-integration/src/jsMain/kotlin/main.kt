/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import hu.simplexion.rui.runtime.dom.RuiDOMAdapter
import hu.simplexion.rui.runtime.dom.html.Button
import hu.simplexion.rui.runtime.dom.html.Text
import hu.simplexion.rui.runtime.rui

fun main() {
    rui(RuiDOMAdapter()) {
        var counter = 0
        Button("Counter: $counter") { counter++ }
        Text("You've clicked $counter times.")
    }
}

//@Rui
//fun test() {
//    T0()
//}

//open class RuiTest<BT : RuiDOMBridge>(
//    override var ruiAdapter: RuiAdapter<BT>,
//    override var ruiParent: RuiFragment<BT>?,
//    override var ruiExternalPatch: Function1<RuiFragment<BT>, Unit>
//) : RuiFragment<BT> {
//
//    var ruiFragment: RuiFragment<BT>
//
//    override fun ruiCreate() {
//        ruiFragment.ruiCreate()
//    }
//
//    override fun ruiMount(bridge: RuiBridge<BT>) {
//        ruiFragment.ruiMount(bridge = bridge)
//    }
//
//    override fun ruiPatch() {
//        val tmp0: RuiFragment<BT> = ruiFragment
//        tmp0.ruiExternalPatch.invoke(tmp0)
//        ruiFragment.ruiPatch()
//    }
//
//    override fun ruiDispose() {
//        ruiFragment.ruiDispose()
//    }
//
//    override fun ruiUnmount(bridge: RuiBridge<BT>) {
//        ruiFragment.ruiUnmount(bridge = bridge)
//    }
//
//    fun ruiEp602(it: RuiFragment<BT>) {
//        it as RuiT0<BT> /*~> Unit */
//    }
//
//    init {
//        ruiFragment = RuiT0(ruiAdapter = ruiAdapter, ruiParent = ruiParent, ruiExternalPatch = ::ruiEp602)
//    }
//
//}