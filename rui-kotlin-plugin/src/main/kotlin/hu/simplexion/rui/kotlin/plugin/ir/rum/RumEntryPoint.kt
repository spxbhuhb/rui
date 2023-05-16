/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiEntryPointBuilder
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class RumEntryPoint(
    val rumClass: RumClass,
    val irFunction: IrSimpleFunction,
) : RumElement {

    val builder = RuiEntryPointBuilder(rumClass.builder, this)

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitEntryPoint(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) {

    }
}