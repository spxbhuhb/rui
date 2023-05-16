/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBlock
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

class RuiBlockBuilder(
    override val ruiClassBuilder: RuiClassBuilder,
    val rumBlock: RumBlock
) : RuiFragmentBuilder {

    // we have to initialize this in build, after all other classes in the module are registered
    override lateinit var symbolMap: RuiClassSymbols

    override fun buildDeclarations() {
        tryBuild(rumBlock.irBlock) {
            symbolMap = ruiContext.ruiSymbolMap.getSymbolMap(RUI_FQN_BLOCK_CLASS)

            rumBlock.statements.forEach {
                it.builder.buildDeclarations()
            }
        }
    }

    override fun irNewInstance(): IrExpression =
        IrConstructorCallImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            symbolMap.defaultType,
            symbolMap.primaryConstructor.symbol,
            0, 0,
            RUI_BLOCK_ARGUMENT_COUNT // adapter, array of fragments
        ).also { constructorCall ->

            constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER, ruiClassBuilder.adapterPropertyBuilder.irGetValue())
            constructorCall.putValueArgument(RUI_BLOCK_ARGUMENT_INDEX_FRAGMENTS, buildFragmentVarArg())

        }


    fun buildFragmentVarArg(): IrExpression {
        return IrVarargImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            irBuiltIns.arrayClass.typeWith(ruiContext.ruiFragmentType),
            ruiContext.ruiFragmentType,
        ).also { vararg ->
            rumBlock.statements.forEach { statement ->
                vararg.addElement(statement.builder.irNewInstance())
            }
        }
    }
}