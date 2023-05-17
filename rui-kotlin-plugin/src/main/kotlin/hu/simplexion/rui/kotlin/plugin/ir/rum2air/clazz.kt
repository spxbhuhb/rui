package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.air.AirClass
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.Name

fun RumClass.toAir(context: RuiPluginContext) {

    val irClass = context.irContext.irFactory.buildClass {
        startOffset = originalFunction.startOffset
        endOffset = originalFunction.endOffset
        origin = IrDeclarationOrigin.DEFINED
        name = this@toAir.name
        kind = ClassKind.CLASS
        visibility = originalFunction.visibility
        modality = Modality.OPEN
    }

    return with(ClassBoundIrBuilder(context, irClass)) {
        toAir()
    }
}

context(ClassBoundIrBuilder)
fun RumClass.toAir(): AirClass {

    val constructor = addConstructor()

    val adapter = constructor.addPropertyParameter(RUI_ADAPTER, classBoundAdapterType, overridden = context.ruiAdapter)
    val scope = constructor.addPropertyParameter(RUI_SCOPE, classBoundFragmentType.makeNullable(), overridden = context.ruiScope)
    val externalPatch = constructor.addPropertyParameter(RUI_EXTERNAL_PATCH, classBoundExternalPatchType, overridden = context.ruiExternalPatch)

    val fragment = addProperty(RUI_FRAGMENT.name, context.ruiFragmentType, propertyIsVar = false, overridden = context.ruiFragment)

    return AirClass(
        originalFunction,
        irClass,
        adapter,
        scope,
        externalPatch,
        fragment,
        addPatch(),
        constructor,
        addInitializer(),
        mutableListOf(),
        mutableListOf()
    )
}

context(ClassBoundIrBuilder)
private fun addConstructor(): IrConstructor =
    irClass.addConstructor {

        isPrimary = true
        returnType = irClass.typeWith()

    }.apply {

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
private fun IrConstructor.addPropertyParameter(
    propertyName: String,
    propertyType: IrType,
    propertyIsVar: Boolean = false,
    overridden: List<IrPropertySymbol>? = null
): IrProperty =
    addValueParameter {
        name = propertyName.name
        type = propertyType
    }.let {
        addProperty(
            propertyName.name,
            propertyType,
            propertyIsVar,
            irGet(it),
            overridden
        )
    }

context(ClassBoundIrBuilder)
private fun addInitializer(): IrAnonymousInitializer =
    irFactory.createAnonymousInitializer(
        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
        origin = IrDeclarationOrigin.DEFINED,
        symbol = IrAnonymousInitializerSymbolImpl(),
        isStatic = false
    ).apply {
        parent = irClass
        body = irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
    }

context(ClassBoundIrBuilder)
private fun addPatch(): IrSimpleFunction =
    irFactory.buildFun {
        name = RUI_PATCH.name
        returnType = irBuiltIns.unitType
        modality = Modality.OPEN
    }.also { function ->

        function.overriddenSymbols = context.ruiPatch
        function.parent = irClass

        function.addDispatchReceiver {
            type = irClass.typeWith(irClass.typeParameters.first().defaultType)
        }

        function.addValueParameter {
            name = Name.identifier("scopeMask")
            type = irBuiltIns.longType
        }

        irClass.declarations += function
    }
