package hu.simplexion.rui.kotlin.plugin.ir.air

import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

interface AirBuilder : AirFunction {

    val externalPatch: IrSimpleFunctionSymbol
    val subBuilders: List<AirBuilder>

}