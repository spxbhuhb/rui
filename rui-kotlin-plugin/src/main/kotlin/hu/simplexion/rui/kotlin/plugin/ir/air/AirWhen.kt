package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirWhen(
    externalPatch: IrSimpleFunction,
    newInstance: IrConstructorCall,
    select: IrSimpleFunction,
    branches: List<IrSimpleFunction>
) : AirRenderingStatement(externalPatch, newInstance) {
}