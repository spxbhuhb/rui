/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.runtime.dom.html

import hu.simplexion.rui.runtime.RuiAdapter
import hu.simplexion.rui.runtime.RuiBridge
import hu.simplexion.rui.runtime.RuiFragment
import org.w3c.dom.Node

/**
 * Leaf nodes in HTML are nodes that does not have any children. A text or an image
 * is a good example.
 */
abstract class LeafNode(
    override val ruiAdapter: RuiAdapter<Node>,
    override val ruiExternalPatch: (it: RuiFragment<Node>) -> Unit,
) : RuiFragment<Node>, RuiBridge<Node> {


    override fun remove(child: RuiBridge<Node>) {
        throw IllegalStateException()
    }

    override fun replace(oldChild: RuiBridge<Node>, newChild: RuiBridge<Node>) {
        throw IllegalStateException()
    }

    override fun add(child: RuiBridge<Node>) {
        throw IllegalStateException()
    }

    override fun ruiMount(bridge: RuiBridge<Node>) {
        bridge.add(this)
    }

    override fun ruiUnmount(bridge: RuiBridge<Node>) {
        bridge.remove(this)
    }

    override fun ruiDispose() {

    }
}