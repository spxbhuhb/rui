package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air.AirEntryPoint
import org.jetbrains.kotlin.ir.declarations.IrClass

fun AirEntryPoint.toRir(context: RuiPluginContext): IrClass =
    with(ClassBoundIrBuilder(context, airClass.irClass)) {
        toRir()
        irClass
    }

context(ClassBoundIrBuilder)
fun AirEntryPoint.toRir() {
    TODO()
}