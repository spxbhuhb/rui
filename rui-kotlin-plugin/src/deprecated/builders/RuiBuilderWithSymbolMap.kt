/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.expressions.IrExpression

@Deprecated("move to IR-RUM-AIR-IR")
interface RuiBuilderWithSymbolMap : RuiBuilder {

    val symbolMap: RuiClassSymbols
        get() = throw IllegalStateException()

    fun IrBlockBuilder.irTraceGet(index: Int, receiver: IrExpression): IrExpression =
        symbolMap.getStateVariable(index).property.backingField
            ?.let { irGetField(receiver, it) }
            ?: irString("?")
}