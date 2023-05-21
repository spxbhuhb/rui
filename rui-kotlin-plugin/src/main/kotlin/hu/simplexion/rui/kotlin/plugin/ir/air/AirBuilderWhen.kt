package hu.simplexion.rui.kotlin.plugin.ir.air

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumWhen
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

class AirBuilderWhen(
    override val rumElement: RumWhen,
    override val irFunction: IrSimpleFunction,
    override val symbolMap: RuiClassSymbols,
    override val externalPatch: IrSimpleFunctionSymbol,
    override val subBuilders: List<AirBuilderBlock>
) : AirBuilder {

    override fun toIr(parent: ClassBoundIrBuilder) = TODO()

}