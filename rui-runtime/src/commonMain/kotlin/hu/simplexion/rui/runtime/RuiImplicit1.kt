/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime

@Suppress("unused")
class RuiImplicit1<BT, VT>(
    override val ruiAdapter: RuiAdapter<BT>,
    override val ruiScope: RuiFragment<BT>,
    override val ruiExternalPatch: RuiExternalPathType<BT>,
    override val ruiFragment: RuiFragment<BT>,
    var v0: VT
) : RuiGeneratedFragment<BT> {

    var ruiDirty0 = 0L

    @Suppress("unused")
    fun ruiInvalidate0(mask: Long) {
        ruiDirty0 = ruiDirty0 or mask
    }

    override fun ruiPatch(scopeMask: Long) {
        val extendedScopeMask = ruiFragment.ruiExternalPatch(ruiFragment, scopeMask)
        if (extendedScopeMask != 0L) ruiFragment.ruiPatch(extendedScopeMask)
    }
}