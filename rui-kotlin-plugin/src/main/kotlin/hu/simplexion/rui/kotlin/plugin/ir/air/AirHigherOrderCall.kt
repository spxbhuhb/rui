package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirHigherOrderCall(
    externalPatch: IrSimpleFunction,
    newInstance: IrConstructorCall,
    higherOrderArguments: List<AirHigherOrderArgument>
) : AirRenderingStatement(externalPatch, newInstance) {
}