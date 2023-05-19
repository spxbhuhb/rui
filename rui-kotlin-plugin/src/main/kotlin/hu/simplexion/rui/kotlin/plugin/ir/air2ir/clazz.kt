package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air.AirClass
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors

fun AirClass.toRir(context: RuiPluginContext): IrClass =
    with(ClassBoundIrBuilder(context, this)) {
        toRir()
        irClass
    }

context(ClassBoundIrBuilder)
fun AirClass.toRir() {
    constructor.toRir()
    initializer.toRir()
}

context(ClassBoundIrBuilder)
fun IrConstructor.toRir() {
    body = irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).apply {

        statements += IrDelegatingConstructorCallImpl.fromSymbolOwner(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            irBuiltIns.anyType,
            irBuiltIns.anyClass.constructors.first(),
            typeArgumentsCount = 0,
            valueArgumentsCount = 0
        )

        statements += IrInstanceInitializerCallImpl(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            irClass.symbol,
            irBuiltIns.unitType
        )
    }
}

context(ClassBoundIrBuilder)
fun IrAnonymousInitializer.toRir() {
    body = irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
    body.statements += airClass.rumElement.initializerStatements
    //body.statements += fragment.irSetField(<ircall builder>, <this>)

    // The initializer has to be the last, so it will be able to access all properties
    irClass.declarations += this

}