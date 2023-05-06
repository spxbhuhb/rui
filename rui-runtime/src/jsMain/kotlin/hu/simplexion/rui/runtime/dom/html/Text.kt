/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
@file:Suppress("FunctionName")

package hu.simplexion.rui.runtime.dom.html

import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.RuiAdapter
import hu.simplexion.rui.runtime.RuiFragment
import hu.simplexion.rui.runtime.RuiPublicApi
import org.w3c.dom.Node

@Rui
@RuiPublicApi
fun Text(content: String) {
}

@RuiPublicApi
class RuiText(
    ruiAdapter: RuiAdapter<Node>,
    ruiParent: RuiFragment<Node>?,
    ruiExternalPatch: (it: RuiFragment<Node>) -> Unit,
    var content: String
) : LeafNode(ruiAdapter, ruiExternalPatch) {

    override val receiver = org.w3c.dom.Text()

    var ruiDirty0 = 0

    override val ruiParent: RuiFragment<Node>
        get() = TODO("Not yet implemented")

    @RuiPublicApi
    fun ruiInvalidate0(mask: Int) {
        ruiDirty0 = ruiDirty0 or mask
    }

    override fun ruiCreate() {
        receiver.data = content
    }

    override fun ruiPatch() {
        if (ruiDirty0 and 1 != 0) {
            receiver.data = content
        }
    }

}