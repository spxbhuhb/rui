package hu.simplexion.rui.kotlin.plugin.ir.rum2air

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.air.AirClass
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrTypeParameterImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.Variance

class RumClass2Air(
    context: RuiPluginContext,
    val rumClass: RumClass
) : ClassBoundIrBuilder(context) {

    fun toAir(): AirClass {

        val originalFunction = rumClass.originalFunction

        irClass = context.irContext.irFactory.buildClass {
            startOffset = originalFunction.startOffset
            endOffset = originalFunction.endOffset
            origin = IrDeclarationOrigin.DEFINED
            name = rumClass.name
            kind = ClassKind.CLASS
            visibility = originalFunction.visibility
            modality = Modality.OPEN
        }

        typeParameters()

        irClass.parent = originalFunction.file
        irClass.superTypes = listOf(context.ruiGeneratedFragmentClass.typeWith(irClass.typeParameters.first().defaultType))
        irClass.metadata = rumClass.originalFunction.metadata

        thisReceiver()
        val constructor = constructor()
        val patch = patch()

        val adapter = addPropertyWitConstructorParameter(RUI_ADAPTER.name, classBoundAdapterType, overridden = context.ruiAdapter)
        val scope = addPropertyWitConstructorParameter(RUI_SCOPE.name, classBoundFragmentType.makeNullable(), overridden = context.ruiScope)
        val externalPatch = addPropertyWitConstructorParameter(RUI_EXTERNAL_PATCH.name, classBoundExternalPatchType, overridden = context.ruiExternalPatch)

        val fragment = addIrProperty(RUI_FRAGMENT.name, context.ruiFragmentType, inIsVar = false, overridden = context.ruiFragment)

        val initializer = initializer()

        create()
        mount()
        unmount()
        dispose()

        airClass = AirClass(
            rumClass,
            irClass,
            adapter,
            scope,
            externalPatch,
            fragment,
            constructor,
            initializer,
            patch
        )

        airClass.builder = rumClass.rendering.toAir(this)
        airClass.stateVariableList = rumClass.stateVariables.map { it.toAir(this@RumClass2Air) }
        airClass.stateVariableMap = airClass.stateVariableList.associateBy { it.rumElement.originalName }
        airClass.dirtyMasks = rumClass.dirtyMasks.map { it.toAir(this@RumClass2Air) }

        return airClass
    }

    private fun typeParameters() {
        irClass.typeParameters = listOf(
            IrTypeParameterImpl(
                SYNTHETIC_OFFSET,
                SYNTHETIC_OFFSET,
                IrDeclarationOrigin.BRIDGE_SPECIAL,
                IrTypeParameterSymbolImpl(),
                Name.identifier(RUI_BT),
                index = 0,
                isReified = false,
                variance = Variance.IN_VARIANCE,
                factory = irFactory
            ).also {
                it.parent = irClass
                it.superTypes = listOf(irBuiltIns.anyNType)
            }
        )
    }

    private fun thisReceiver(): IrValueParameter =

        irFactory.createValueParameter(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            IrDeclarationOrigin.INSTANCE_RECEIVER,
            IrValueParameterSymbolImpl(),
            SpecialNames.THIS,
            UNDEFINED_PARAMETER_INDEX,
            IrSimpleTypeImpl(irClass.symbol, false, emptyList(), emptyList()),
            varargElementType = null,
            isCrossinline = false,
            isNoinline = false,
            isHidden = false,
            isAssignable = false
        ).also {
            it.parent = irClass
            irClass.thisReceiver = it
        }

    private fun constructor(): IrConstructor =

        irClass.addConstructor {
            isPrimary = true
            returnType = irClass.typeWith()
        }.apply {
            parent = irClass

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

    private fun initializer(): IrAnonymousInitializer =

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

    private fun create(): IrSimpleFunction =

        irFactory.buildFun {
            name = Name.identifier(RUI_CREATE)
            returnType = irBuiltIns.unitType
            modality = Modality.OPEN
            isFakeOverride = true
        }.also { function ->

            function.overriddenSymbols = context.ruiCreate
            function.parent = irClass

            function.addDispatchReceiver {
                type = irClass.typeWith(irClass.typeParameters.first().defaultType)
            }

            irClass.declarations += function
        }

    @Suppress("DuplicatedCode")  // I don't want to merge mount and unmount
    private fun mount(): IrSimpleFunction =

        irFactory.buildFun {
            name = Name.identifier(RUI_MOUNT)
            returnType = irBuiltIns.unitType
            modality = Modality.OPEN
            isFakeOverride = true
        }.also { function ->

            function.overriddenSymbols = context.ruiMount
            function.parent = irClass

            function.addDispatchReceiver {
                type = irClass.typeWith(irClass.typeParameters.first().defaultType)
            }

            function.addValueParameter {
                name = Name.identifier("bridge")
                type = context.ruiBridgeType
            }

            irClass.declarations += function
        }

    private fun patch(): IrSimpleFunction =

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

    @Suppress("DuplicatedCode")  // I don't want to merge mount and unmount
    private fun unmount(): IrSimpleFunction =

        irFactory.buildFun {
            name = Name.identifier(RUI_UNMOUNT)
            returnType = irBuiltIns.unitType
            modality = Modality.OPEN
            isFakeOverride = true
        }.also { function ->

            function.overriddenSymbols = context.ruiUnmount
            function.parent = irClass

            function.addDispatchReceiver {
                type = irClass.typeWith(irClass.typeParameters.first().defaultType)
            }

            function.addValueParameter {
                name = Name.identifier("bridge")
                type = context.ruiBridgeType
            }

            irClass.declarations += function
        }

    private fun dispose(): IrSimpleFunction =

        irFactory.buildFun {
            name = Name.identifier(RUI_DISPOSE)
            returnType = irBuiltIns.unitType
            modality = Modality.OPEN
            isFakeOverride = true
        }.also { function ->

            function.overriddenSymbols = context.ruiDispose
            function.parent = irClass

            function.addDispatchReceiver {
                type = irClass.typeWith(irClass.typeParameters.first().defaultType)
            }

            irClass.declarations += function
        }

}