package hu.simplexion.rui.kotlin.plugin.ir.sir2ir

import hu.simplexion.rui.kotlin.plugin.ir.RUI_BLOCK_ARGUMENT_INDEX_FRAGMENTS
import hu.simplexion.rui.kotlin.plugin.ir.RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER
import hu.simplexion.rui.kotlin.plugin.ir.sir.SirBlock
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET


context(ClassBoundIrBuilder)
fun SirBlock.final(getAdapter: IrExpression) {
    with(this) {
        newInstance.final(getAdapter)
    }
}

context(ClassBoundIrBuilder, SirBlock)
private fun IrConstructorCall.final(getAdapter: IrExpression) {
    putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER, getAdapter)
    putValueArgument(RUI_BLOCK_ARGUMENT_INDEX_FRAGMENTS, buildFragmentVarArg())
}

context(ClassBoundIrBuilder, SirBlock)
private fun buildFragmentVarArg(): IrExpression {
    return IrVarargImpl(
        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
        irBuiltIns.arrayClass.typeWith(context.ruiFragmentType),
        context.ruiFragmentType,
    ).also { vararg ->
        statements.forEach { statement ->
            vararg.addElement(statement.newInstance)
        }
    }
}