package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

open class AirRenderingStatement(
    externalPatch: IrSimpleFunction,
    newInstance: IrConstructorCall
) : AirElement {
}