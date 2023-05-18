package hu.simplexion.rui.kotlin.plugin.ir.sir2ir

import hu.simplexion.rui.kotlin.plugin.ir.sir.SirClass
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors


context(ClassBoundIrBuilder)
fun SirClass.finalize() {
    constructor.finalize()
    initializer.finalize()
}

context(ClassBoundIrBuilder)
fun IrConstructor.finalize() {
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
fun IrAnonymousInitializer.finalize() {
    irClass.declarations += this
    body = irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
}