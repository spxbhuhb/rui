/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
@file:Suppress("FunctionName", "UNUSED_PARAMETER")

package hu.simplexion.rui.runtime.dom.html

import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.RuiAdapter
import hu.simplexion.rui.runtime.RuiFragment
import hu.simplexion.rui.runtime.RuiPublicApi
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.Node
import org.w3c.dom.events.MouseEvent

@Rui
@RuiPublicApi
fun Button(title: String, onClick: () -> Unit) {
}

@RuiPublicApi
class RuiButton(
    ruiAdapter: RuiAdapter<Node>,
    ruiScope: RuiFragment<Node>?,
    ruiExternalPatch: (it: RuiFragment<Node>) -> Unit,
    var label: String,
    var onClick: (MouseEvent) -> Unit
) : LeafNode(ruiAdapter, ruiExternalPatch) {

    override val receiver = document.createElement("button") as HTMLButtonElement

    var ruiDirty0 = 0

    override val ruiScope: RuiFragment<Node>
        get() = TODO("Not yet implemented")

    @RuiPublicApi
    fun ruiInvalidate0(mask: Int) {
        ruiDirty0 = ruiDirty0 or mask
    }

    override fun ruiCreate() {
        receiver.innerText = label
        receiver.onclick = onClick
    }

    override fun ruiPatch() {
        if (ruiDirty0 and 1 != 0) {
            receiver.innerText = label
        }
        if (ruiDirty0 and 2 != 0) {
            receiver.onclick = onClick
        }
    }

}