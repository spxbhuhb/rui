/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime

@Suppress("unused")
class RuiImplicit1<BT, VT>(
    override val ruiAdapter: RuiAdapter<BT>,
    override val ruiScope: RuiFragment<BT>,
    override val ruiExternalPatch: (it: RuiFragment<BT>) -> Unit,
    override val ruiFragment: RuiFragment<BT>,
    var v0: VT
) : RuiGeneratedFragment<BT> {

    // TODO fix RuiImplicit1 dirty mask and patch

    override fun ruiPatch() {
        ruiFragment.ruiExternalPatch(ruiFragment)
        ruiFragment.ruiPatch()
    }

}