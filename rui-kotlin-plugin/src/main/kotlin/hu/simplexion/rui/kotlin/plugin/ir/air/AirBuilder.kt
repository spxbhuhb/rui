package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumRenderingStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

abstract class AirBuilder(
    override val rumElement: RumRenderingStatement,
    override val irFunction: IrSimpleFunction,
    val symbolMap: RuiClassSymbols,
    val externalPatch: AirExternalPatch,
    val subBuilders: List<AirBuilder>
) : AirFunction {

    abstract fun toIr(parent: ClassBoundIrBuilder)

}