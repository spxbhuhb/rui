package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.AirClass2Ir
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

    val patch: IrSimpleFunction,

    val stateVariables: List<AirStateVariable>,
    val dirtyMasks: List<AirDirtyMask>

) : AirElement {

    lateinit var builder: AirBuilder

    fun toIr(context: RuiPluginContext): IrClass = AirClass2Ir(context, this).toIr()

}