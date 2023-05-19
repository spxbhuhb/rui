package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import org.jetbrains.kotlin.ir.declarations.*

class SirClass(

    val originalFunction: IrFunction,
    override val rumElement: RumClass,

    val irClass: IrClass,

    val adapter: IrProperty,
    val scope: IrProperty,
    val externalPatch: IrProperty,
    val fragment: IrProperty,

    val constructor: IrConstructor,
    val initializer: IrAnonymousInitializer,

    val stateVariables: List<SirStateVariable>,
    val dirtyMasks: List<SirDirtyMask>,

    val patch: IrSimpleFunction,
    val builder: IrSimpleFunction

) : SirElement