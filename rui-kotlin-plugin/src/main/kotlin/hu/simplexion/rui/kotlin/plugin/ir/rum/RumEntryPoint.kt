/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air.AirEntryPoint
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.rum2air.RumEntryPoint2Air
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiEntryPointBuilder
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class RumEntryPoint(
    val rumClass: RumClass,
    val irFunction: IrSimpleFunction,
) : RumElement {

    val builder = RuiEntryPointBuilder(rumClass.builder, this)

    fun toAir(context: RuiPluginContext): AirEntryPoint = RumEntryPoint2Air(context, this).toAir()

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitEntryPoint(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) {

    }
}