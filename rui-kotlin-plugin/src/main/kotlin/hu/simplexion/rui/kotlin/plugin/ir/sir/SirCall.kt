package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

class SirCall(
    newInstance: IrConstructorCall,
    val externalPatch: IrSimpleFunction,
    val callSiteDependencyMask: Long
) : SirRenderingStatement(newInstance) {
}