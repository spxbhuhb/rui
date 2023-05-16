package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirCall(
    externalPatch: IrSimpleFunction,
    newInstance: IrConstructorCall,
    callSiteDependencyMask: Long
) : AirRenderingStatement(externalPatch, newInstance) {
}