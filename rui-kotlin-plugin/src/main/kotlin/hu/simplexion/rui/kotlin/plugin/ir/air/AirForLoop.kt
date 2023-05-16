package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression

class AirForLoop(
    externalPatch: IrSimpleFunction,
    newInstance: IrConstructorCall,
    iterator: IrExpression,
    loopVariable: IrDeclaration,
    implicitExternalPatch: IrSimpleFunction,
    builder: IrSimpleFunction
) : AirRenderingStatement(externalPatch, newInstance) {
}