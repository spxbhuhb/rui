/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime

class RuiImplicit0<BT>(
    override val ruiAdapter: RuiAdapter<BT>,
    override val ruiScope: RuiFragment<BT>,
    override val ruiExternalPatch: (it: RuiFragment<BT>) -> Unit,
) : RuiGeneratedFragment<BT> {

    override lateinit var ruiFragment: RuiFragment<BT>

    override fun ruiPatch() {
        ruiFragment.ruiExternalPatch(ruiFragment)
        ruiFragment.ruiPatch()
    }
}