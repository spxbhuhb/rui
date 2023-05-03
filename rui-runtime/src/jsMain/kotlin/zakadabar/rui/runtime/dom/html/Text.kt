/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package zakadabar.rui.runtime.dom.html

import org.w3c.dom.Node
import zakadabar.rui.runtime.Rui
import zakadabar.rui.runtime.RuiAdapter
import zakadabar.rui.runtime.RuiFragment
import zakadabar.rui.runtime.RuiPublicApi

@Rui
fun Text(content: String) {
}

class RuiText(
    ruiAdapter: RuiAdapter<Node>,
    ruiExternalPatch: (it: RuiFragment<Node>) -> Unit,
    var content: String
) : LeafNode(ruiAdapter, ruiExternalPatch) {

    override val receiver = org.w3c.dom.Text()

    var ruiDirty0 = 0

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