package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

class SirBlock(
    newInstance: IrConstructorCall,
    val statements: List<SirRenderingStatement>
) : SirRenderingStatement(newInstance) {
}