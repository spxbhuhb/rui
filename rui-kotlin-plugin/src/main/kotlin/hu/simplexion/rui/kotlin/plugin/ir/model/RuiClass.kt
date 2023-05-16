/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.toRuiClassFqName
import hu.simplexion.rui.kotlin.plugin.ir.transform.builders.RuiClassBuilder
import hu.simplexion.rui.kotlin.plugin.ir.transform.fromir.RuiBoundaryVisitor
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.statements

class RuiClass(
    val ruiContext: RuiPluginContext,
    val irFunction: IrFunction,
) : RuiElement {

    val name = irFunction.toRuiClassFqName(ruiContext).shortName()

    val boundary = RuiBoundaryVisitor(ruiContext).findBoundary(irFunction)

    val originalStatements = checkNotNull(irFunction.body?.statements) { "missing function body" }

    val initializerStatements = mutableListOf<IrStatement>()
    val renderingStatements = mutableListOf<IrStatement>()

    val stateVariables = mutableMapOf<String, RuiStateVariable>()
    val dirtyMasks = mutableListOf<RuiDirtyMask>()

    lateinit var rootBlock: RuiStatement

    val symbolMap = mutableMapOf<IrSymbol, RuiElement>()

    val builder = RuiClassBuilder(this)

    val irClass
        get() = builder.irClass

    internal fun stateVariableByGetterOrNull(symbol: IrSymbol): RuiStateVariable? =
        symbolMap[symbol]?.let {
            if (it is RuiStateVariable && it.builder.getter.symbol == symbol) it else null
        }

    override fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R =
        visitor.visitClass(this, data)

    override fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D) {
        stateVariables.values.forEach { it.accept(visitor, data) }
        dirtyMasks.forEach { it.accept(visitor, data) }
        rootBlock.accept(visitor, data)
    }
}