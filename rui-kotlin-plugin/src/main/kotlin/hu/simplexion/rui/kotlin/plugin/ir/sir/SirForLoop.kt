package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

class SirForLoop(
    newInstance: IrConstructorCall,
    val iterator: IrExpression,
    val loopVariable: IrDeclaration,
    val implicitExternalPatch: IrSimpleFunction,
    val builder: IrSimpleFunction
) : SirRenderingStatement(newInstance) {
}