package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

class SirHigherOrderCall(
    newInstance: IrConstructorCall,
    higherOrderArguments: List<SirHigherOrderArgument>
) : SirRenderingStatement(newInstance) {
}