/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime

/**
 * Interface to implement by fragments generated by the compiler plugin.
 * This interface may define functions the generated fragments use, so
 * we do not have to code them in IR.
 */
@RuiPublicApi
interface RuiGeneratedFragment<BT> : RuiFragment<BT> {

    // FIXME code generation uses RuiFragment at the moment, check if it is better to use this RuiGeneratedFragment

    val fragment: RuiFragment<BT>

    override fun ruiCreate() {
        fragment.ruiCreate()
    }

    override fun ruiMount(bridge: RuiBridge<BT>) {
        fragment.ruiMount(bridge)
    }

    override fun ruiPatch() {
        fragment.ruiPatch()
    }

    override fun ruiUnmount(bridge: RuiBridge<BT>) {
        fragment.ruiUnmount(bridge)
    }

    override fun ruiDispose() {
        fragment.ruiDispose()
    }

}