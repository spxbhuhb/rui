/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.ir2rum.BoundaryVisitor
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import hu.simplexion.rui.kotlin.plugin.ir.toRuiClassFqName
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiClassBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.statements

class RumClass(
    val ruiContext: RuiPluginContext,
    val originalFunction: IrFunction,
) : RumElement {

    val name = originalFunction.toRuiClassFqName(ruiContext).shortName()

    val boundary = BoundaryVisitor(ruiContext).findBoundary(originalFunction)

    val originalStatements = checkNotNull(originalFunction.body?.statements) { "missing function body" }

    val initializerStatements = mutableListOf<IrStatement>()
    val renderingStatements = mutableListOf<IrStatement>()

    val stateVariables = mutableMapOf<String, RumStateVariable>()
    val dirtyMasks = mutableListOf<RumDirtyMask>()

    lateinit var rootBlock: RumStatement

    val symbolMap = mutableMapOf<IrSymbol, RumElement>()

    val builder = RuiClassBuilder(this)

    val irClass
        get() = builder.irClass

    internal fun stateVariableByGetterOrNull(symbol: IrSymbol): RumStateVariable? =
        symbolMap[symbol]?.let {
            if (it is RumStateVariable && it.builder.getter.symbol == symbol) it else null
        }

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitClass(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) {
        stateVariables.values.forEach { it.accept(visitor, data) }
        dirtyMasks.forEach { it.accept(visitor, data) }
        rootBlock.accept(visitor, data)
    }
}