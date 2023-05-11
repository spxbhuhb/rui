/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime

class RuiImplicit0<BT>(
    override val ruiAdapter: RuiAdapter<BT>,
    override val ruiScope: RuiFragment<BT>,
    override val ruiExternalPatch: RuiExternalPathType<BT>,
) : RuiGeneratedFragment<BT> {

    override lateinit var ruiFragment: RuiFragment<BT>

    override fun ruiPatch(scopeMask: Long) {
        val extendedScopeMask = ruiFragment.ruiExternalPatch(ruiFragment, scopeMask)
        if (extendedScopeMask != 0L) ruiFragment.ruiPatch(extendedScopeMask)
    }
}