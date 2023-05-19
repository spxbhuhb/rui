package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import org.jetbrains.kotlin.ir.declarations.*

class AirClass(

    val originalFunction: IrFunction,
    override val rumElement: RumClass,

    val irClass: IrClass,

    val adapter: IrProperty,
    val scope: IrProperty,
    val externalPatch: IrProperty,
    val fragment: IrProperty,

    val constructor: IrConstructor,
    val initializer: IrAnonymousInitializer,

    val builder: IrSimpleFunction,
    val patch: IrSimpleFunction,

    val properties: MutableList<AirProperty>,
    val functions: MutableList<AirFunction>

) : AirElement