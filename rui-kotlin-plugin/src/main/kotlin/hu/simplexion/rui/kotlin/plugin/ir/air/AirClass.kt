package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import org.jetbrains.kotlin.ir.declarations.*

class AirClass(
    val originalFunction: IrFunction,
    val rumClass: RumClass,

    val irClass: IrClass,

    val adapter: IrProperty,
    val scope: IrProperty,
    val externalPatch: IrProperty,
    val fragment: IrProperty,

    val constructor: IrConstructor,
    val initializer: IrAnonymousInitializer,

    val stateVariables: List<AirStateVariable>,
    val dirtyMasks: List<AirDirtyMask>,

    val patch: IrSimpleFunction,
    val builder: IrSimpleFunction

) : AirElement