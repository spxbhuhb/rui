package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

class SirWhen(
    newInstance: IrConstructorCall,
    val select: IrSimpleFunction,
    val branches: List<IrSimpleFunction>
) : SirRenderingStatement(newInstance) {
}