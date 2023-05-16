package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class AirClass(
    val originalFunction: IrSimpleFunction,
    val irClass: IrClass,
    val adapter: IrProperty,
    val scope: IrProperty,
    val externalPatch: IrProperty,
    val fragment: IrProperty,
    val patch: IrSimpleFunction,
    val constructor: IrSimpleFunction,
    val initializer: IrSimpleFunction,
    val stateVariables: List<AirStateVariable>,
    val dirtyMasks: List<AirDirtyMask>,
    val rendering: AirRenderingStatement
) : AirElement {
}