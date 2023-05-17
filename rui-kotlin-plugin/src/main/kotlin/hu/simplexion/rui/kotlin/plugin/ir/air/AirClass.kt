package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.ir.declarations.*

class AirClass(
    val originalFunction: IrFunction,
    val irClass: IrClass,
    val adapter: IrProperty,
    val scope: IrProperty,
    val externalPatch: IrProperty,
    val fragment: IrProperty,
    val patch: IrSimpleFunction,
    val constructor: IrConstructor,
    val initializer: IrAnonymousInitializer,
    val stateVariables: List<AirStateVariable>,
    val dirtyMasks: List<AirDirtyMask>
) : AirElement {
    lateinit var rendering: AirRenderingStatement
}