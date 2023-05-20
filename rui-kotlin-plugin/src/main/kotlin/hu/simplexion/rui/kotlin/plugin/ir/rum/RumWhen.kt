/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RUI_WHEN
import hu.simplexion.rui.kotlin.plugin.ir.air.AirBuilder
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiWhenBuilder
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrWhen

class RumWhen(
    rumClass: RumClass,
    index: Int,
    val irSubject: IrVariable?,
    val irWhen: IrWhen
) : RumRenderingStatement(rumClass, index) {

    override val name = "$RUI_WHEN$index"

    val branches = mutableListOf<RumBranch>()

    override val builder = RuiWhenBuilder(rumClass.builder, this)

    override fun toAir(parent: ClassBoundIrBuilder): AirBuilder {
        TODO("Not yet implemented")
    }

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitWhen(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) {
        branches.forEach { it.accept(visitor, data) }
    }
}