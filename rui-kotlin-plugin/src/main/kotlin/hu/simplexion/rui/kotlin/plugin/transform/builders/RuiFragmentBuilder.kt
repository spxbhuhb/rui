/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.builders

import hu.simplexion.rui.kotlin.plugin.transform.RuiClassSymbols
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.expressions.IrExpression

interface RuiFragmentBuilder : RuiBuilder {

    val symbolMap: RuiClassSymbols
        get() = throw IllegalStateException()

    /**
     * Add functions and properties used by this fragment. These depend on the
     * fragment type. External patch, select for branches, builders for loops and
     * higher-order functions etc.
     */
    fun buildDeclarations() {
        throw NotImplementedError()
    }

    /**
     * Create a new instance of the fragment. Called from `RuiClass.build` after
     * all classes in the IR ModuleFragment are transformed into RuiClass. This
     * ensures that references to other Rui classes can be resolved properly.
     */
    fun irNewInstance(): IrExpression {
        throw NotImplementedError()
    }

    fun IrBlockBuilder.irTraceGet(index: Int, receiver: IrExpression): IrExpression =
        symbolMap.getStateVariable(index).property.backingField
            ?.let { irGetField(receiver, it) }
            ?: irString("?")
}