/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.RUI_DIRTY
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiDirtyMaskBuilder
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor
import org.jetbrains.kotlin.name.Name

class RuiDirtyMask(
    val ruiClass: RuiClass,
    val index: Int
) : RuiElement {

    val name = Name.identifier("$RUI_DIRTY$index")

    val builder = RuiDirtyMaskBuilder(ruiClass.builder, this)

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitDirtyMask(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) = Unit

}