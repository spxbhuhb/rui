/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RUI_DIRTY
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.rum2air.RumDirtyMask2Air
import org.jetbrains.kotlin.name.Name

class RumDirtyMask(
    val rumClass: RumClass,
    val index: Int
) : RumElement {

    val name = Name.identifier("$RUI_DIRTY$index")

    fun toAir(parent: ClassBoundIrBuilder) = RumDirtyMask2Air(parent, this).toAir()

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitDirtyMask(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) = Unit

}