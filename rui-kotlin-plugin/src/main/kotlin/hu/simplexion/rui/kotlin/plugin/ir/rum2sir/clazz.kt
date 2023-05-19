package hu.simplexion.rui.kotlin.plugin.ir.rum2sir

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import hu.simplexion.rui.kotlin.plugin.ir.sir.SirClass
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.Name

fun RumClass.toSir(context: RuiPluginContext): SirClass {

    val irClass = context.irContext.irFactory.buildClass {
        startOffset = originalFunction.startOffset
        endOffset = originalFunction.endOffset
        origin = IrDeclarationOrigin.DEFINED
        name = this@toSir.name
        kind = ClassKind.CLASS
        visibility = originalFunction.visibility
        modality = Modality.OPEN
    }

    return with(ClassBoundIrBuilder(context, irClass)) {
        toAir()
    }
}

context(ClassBoundIrBuilder)
private fun RumClass.toAir(): SirClass {

    val constructor = addConstructor()

    val adapter = constructor.addPropertyParameter(RUI_ADAPTER.name, classBoundAdapterType, overridden = context.ruiAdapter)
    val scope = constructor.addPropertyParameter(RUI_SCOPE.name, classBoundFragmentType.makeNullable(), overridden = context.ruiScope)
    val externalPatch = constructor.addPropertyParameter(RUI_EXTERNAL_PATCH.name, classBoundExternalPatchType, overridden = context.ruiExternalPatch)

    val fragment = addProperty(RUI_FRAGMENT.name, context.ruiFragmentType, inIsVar = false, overridden = context.ruiFragment)

    sirClass = SirClass(
        originalFunction,
        this,
        irClass,
        adapter,
        scope,
        externalPatch,
        fragment,
        constructor,
        addInitializer(),
        stateVariables.values.map { it.toSir() },
        dirtyMasks.map { it.toSir() },
        addPatch(),
        builder(irClass.startOffset) // TODO boundary as start of the builder might be better conceptually
    )

    return sirClass
}

context(ClassBoundIrBuilder)
private fun addConstructor(): IrConstructor =
    irClass.addConstructor {
        isPrimary = true
        returnType = irClass.typeWith()
    }.apply {
        parent = irClass
    }

context(ClassBoundIrBuilder)
fun IrConstructor.addPropertyParameter(
    inName: Name,
    inType: IrType,
    inIsVar: Boolean = false,
    overridden: List<IrPropertySymbol>? = null,
    inVarargElementType: IrType? = null
): IrProperty =
    addValueParameter {
        name = inName
        type = inType
        varargElementType = inVarargElementType
    }.let {
        addProperty(
            inName,
            inType,
            inIsVar,
            irGet(it, origin = IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER),
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
        // we should not add the initializer here as it should be the last
        // declaration of the class to be able to access all properties
        // it is added in finalize
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