package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirBlock(
    externalPatch: IrSimpleFunction,
    newInstance: IrConstructorCall,
    statements: List<AirRenderingStatement>
) : AirRenderingStatement(externalPatch, newInstance) {
}