/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.plugin.RuiDumpPoint
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrTypeParameterImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.Variance

@Deprecated("move to IR-RUM-AIR-IR")
class RuiClassBuilder(
    override val rumClass: RumClass
) : RuiBuilder {

    override val ruiClassBuilder: RuiClassBuilder
        get() = this

    override val ruiContext: RuiPluginContext
        get() = rumClass.ruiContext

    val irFunction = rumClass.originalFunction

    val name: String
    val fqName: FqName

    val constructor: IrConstructor
    val initializer: IrAnonymousInitializer
    val thisReceiver: IrValueParameter

    private lateinit var adapterConstructorParameter: IrValueParameter
    private lateinit var scopeConstructorParameter: IrValueParameter
    private lateinit var externalPatchConstructorParameter: IrValueParameter

    val adapterPropertyBuilder: RuiPropertyBuilder
    val scopePropertyBuilder: RuiPropertyBuilder
    val externalPatchPropertyBuilder: RuiPropertyBuilder
    val fragmentPropertyBuilder: RuiPropertyBuilder

    val create: IrSimpleFunction
    val mount: IrSimpleFunction
    val patch: IrSimpleFunction
    val unmount: IrSimpleFunction
    val dispose: IrSimpleFunction

    override val irClass: IrClass = irFactory.buildClass {
        startOffset = irFunction.startOffset
        endOffset = irFunction.endOffset
        origin = IrDeclarationOrigin.DEFINED
        name = rumClass.name
        kind = ClassKind.CLASS
        visibility = irFunction.visibility
        modality = Modality.OPEN
    }

    init {
        initTypeParameters()

        irClass.parent = irFunction.file
        irClass.superTypes = listOf(ruiContext.ruiGeneratedFragmentClass.typeWith(irClass.typeParameters.first().defaultType))
        irClass.metadata = irFunction.metadata

        name = irClass.name.identifier
        fqName = irClass.kotlinFqName

        thisReceiver = initThisReceiver()
        constructor = initConstructor()

        adapterPropertyBuilder = initAdapterProperty()
        scopePropertyBuilder = initScopeProperty()
        externalPatchPropertyBuilder = initExternalPatchProperty()
        fragmentPropertyBuilder = initFragmentProperty()

        initializer = initInitializer()

        val fake = !ruiContext.withTrace // generate fake overrides when trace is not enabled

        create = initRuiFunction(RUI_CREATE, ruiContext.ruiCreate, fake)
        mount = initRuiFunction(RUI_MOUNT, ruiContext.ruiMount, fake)
        patch = initRuiFunction(RUI_PATCH, ruiContext.ruiPatch, false)
        dispose = initRuiFunction(RUI_DISPOSE, ruiContext.ruiDispose, fake)
        unmount = initRuiFunction(RUI_UNMOUNT, ruiContext.ruiUnmount, fake)

    }

    private fun initTypeParameters() {
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

    private fun initThisReceiver(): IrValueParameter {

        val thisReceiver = irFactory.createValueParameter(
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

        return thisReceiver
    }

    private fun initAdapterProperty(): RuiPropertyBuilder =
        RuiPropertyBuilder(ruiClassBuilder, Name.identifier(RUI_ADAPTER), classBoundAdapterType, isVar = false).also {
            it.irField.initializer = irFactory.createExpressionBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, irGet(adapterConstructorParameter))
            it.irProperty.overriddenSymbols = ruiContext.ruiAdapter
        }

    private fun initScopeProperty(): RuiPropertyBuilder =
        RuiPropertyBuilder(ruiClassBuilder, Name.identifier(RUI_SCOPE), classBoundFragmentType.makeNullable(), isVar = false).also {
            it.irField.initializer = irFactory.createExpressionBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, irGet(scopeConstructorParameter))
            it.irProperty.overriddenSymbols = ruiContext.ruiScope
        }

    private fun initExternalPatchProperty(): RuiPropertyBuilder =
        RuiPropertyBuilder(ruiClassBuilder, Name.identifier(RUI_EXTERNAL_PATCH), classBoundExternalPatchType, isVar = false).also {
            it.irField.initializer = irFactory.createExpressionBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, irGet(externalPatchConstructorParameter))
            it.irProperty.overriddenSymbols = ruiContext.ruiExternalPatch
        }

    private fun initFragmentProperty(): RuiPropertyBuilder =
        RuiPropertyBuilder(ruiClassBuilder, Name.identifier(RUI_FRAGMENT), ruiContext.ruiFragmentType, isVar = false).also {
            it.irProperty.overriddenSymbols = ruiContext.ruiFragment
        }

    /**
     * Creates a primary constructor with a standard body (super class constructor call
     * and initializer call).
     *
     * Adds value parameters to the constructor:
     *
     * - `ruiAdapter` with type `RuiAdapter`
     * - `ruiScope` with type `RuiFragment<BT>`
     * - `ruiExternalPatch` with type `(it : RuiFragment) -> Unit`
     *
     * Later, `RuiStateTransformer` adds parameters from the original function.
     */
    private fun initConstructor(): IrConstructor {

        val constructor = irClass.addConstructor {
            isPrimary = true
            returnType = irClass.typeWith()
        }

        adapterConstructorParameter = constructor.addValueParameter {
            name = Name.identifier(RUI_ADAPTER)
            type = ruiContext.ruiAdapterType
        }

        scopeConstructorParameter = constructor.addValueParameter {
            name = Name.identifier(RUI_SCOPE)
            type = classBoundFragmentType.makeNullable()
        }

        externalPatchConstructorParameter = constructor.addValueParameter {
            name = Name.identifier(RUI_EXTERNAL_PATCH)
            type = classBoundExternalPatchType
        }

        constructor.body = irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).apply {

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

        return constructor
    }

    private fun initInitializer(): IrAnonymousInitializer {

        val initializer = irFactory.createAnonymousInitializer(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            origin = IrDeclarationOrigin.DEFINED,
            symbol = IrAnonymousInitializerSymbolImpl(),
            isStatic = false
        )

        initializer.parent = irClass
        initializer.body = irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)

        return initializer
    }

    private fun initRuiFunction(functionName: String, overrides: List<IrSimpleFunctionSymbol>, fake: Boolean = false): IrSimpleFunction =
        irFactory.buildFun {
            name = Name.identifier(functionName)
            returnType = irBuiltIns.unitType
            modality = Modality.OPEN
            isFakeOverride = fake
        }.also { function ->

            function.overriddenSymbols = overrides
            function.parent = irClass

            function.addDispatchReceiver {
                type = irClass.typeWith(irClass.typeParameters.first().defaultType)
            }

            if (functionName == RUI_PATCH) {
                function.addValueParameter {
                    name = Name.identifier("scopeMask")
                    type = irBuiltIns.longType
                }
            }

            if (functionName == RUI_MOUNT || functionName == RUI_UNMOUNT) {
                function.addValueParameter {
                    name = Name.identifier("bridge")
                    type = ruiContext.ruiBridgeType
                }
            }

            irClass.declarations += function
        }

    fun build() {

//        val rootBuilder = rumClass.rootBlock.builder
//
//        rootBuilder.buildDeclarations()

        // we need to build these functions only when trace is enabled, otherwise we can use
        // a fake override and use the version from RuiGeneratedFragment

//        if (ruiContext.withTrace) {
//            buildRuiCall(create, rootBuilder, rootBuilder.symbolMap.create)
//            buildRuiCall(mount, rootBuilder, rootBuilder.symbolMap.mount)
//            buildRuiCall(unmount, rootBuilder, rootBuilder.symbolMap.unmount)
//            buildRuiCall(dispose, rootBuilder, rootBuilder.symbolMap.dispose)
//        }

        // paths has to be generated in all cases because it calls external patch
        // which is class dependent

//        buildRuiCall(patch, rootBuilder, rootBuilder.symbolMap.patch)

        // State initialisation must precede fragment initialisation, so the fragments
        // will get initialized values in their constructor.
        // Individual fragment builders will append their own initialisation code
        // after the state is properly initialized.

        traceInit()

        initializer.body.statements += rumClass.initializerStatements
        //       initializer.body.statements += fragmentPropertyBuilder.irSetField(rootBuilder.irNewInstance())

        // The initializer has to be the last, so it will be able to access all properties
        irClass.declarations += initializer

        RuiDumpPoint.KotlinLike.dump(ruiContext) {
            ruiContext.output("KOTLIN LIKE", irClass.dumpKotlinLike(), irClass)
        }
    }

    fun traceInit() {
        if (!ruiContext.withTrace) return

        val args = mutableListOf<IrExpression>()

        DeclarationIrBuilder(irContext, initializer.symbol).apply {

            constructor.valueParameters.forEach {
                val name = it.name.identifier

                if (name != RUI_ADAPTER && name != RUI_EXTERNAL_PATCH) {
                    args += irString("${it.name}:")
                    args += irGet(it)
                }
            }
        }

        initializer.body.statements += irTrace("init", args)
    }

    /**
     * Builds a function body that calls the same function of the child fragment. Except `ruiPatch` which
     * is more complex. This is called only when there is a trace.
     */
    fun buildRuiCall(function: IrSimpleFunction, rootBuilder: RuiFragmentBuilder, callee: IrSimpleFunction) {
        function.body = DeclarationIrBuilder(irContext, function.symbol).irBlockBody {
            traceRuiCall(function)

            val symbolMap = rootBuilder.symbolMap

            when (callee.name.identifier) {
                RUI_CREATE -> buildRuiCall(symbolMap.create, function, this)
                RUI_MOUNT -> buildRuiCallWithBridge(symbolMap.mount, function, this)
                RUI_PATCH -> this.buildRuiPatch(symbolMap, function)
                RUI_UNMOUNT -> buildRuiCallWithBridge(symbolMap.unmount, function, this)
                RUI_DISPOSE -> buildRuiCall(symbolMap.dispose, function, this)
            }
        }
    }

    private fun IrBlockBodyBuilder.traceRuiCall(function: IrSimpleFunction) {

        if (!ruiContext.withTrace) return

        val name = function.name.identifier

        +when {
            name.startsWith("ruiPatch") -> {

                val args = mutableListOf<IrExpression>()

                args += irConst("scopeMask:")
                args += irGet(function.valueParameters[0])

                rumClass.dirtyMasks.forEach {
                    args += irString("${it.name}:")
                    //                   args += it.builder.propertyBuilder.irGetValue(irGet(function.dispatchReceiverParameter!!))
                }

                irTrace(function, "patch", args)

            }

            else -> irTrace(function, function.name.identifier.substring(3).lowercase(), emptyList())
        }

    }

    /**
     * Fetch the instance of this fragment.
     */
    fun IrBlockBodyBuilder.irGetFragment(scope: IrSimpleFunction): IrExpression {
        // FIXME check receiver logic when having deeper structures
        return irGet(
            fragmentPropertyBuilder.type,
            IrGetValueImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, scope.dispatchReceiverParameter!!.symbol),
            fragmentPropertyBuilder.getter.symbol
        )
    }

    /**
     * RumCall2Air a Rui function of this fragment.
     *
     * @param scope The function we call from.
     */
    fun buildRuiCall(callee: IrSimpleFunction, scope: IrSimpleFunction, builder: IrBlockBodyBuilder) {
        builder.run {
            +irCallOp(
                callee.symbol,
                type = irBuiltIns.unitType,
                dispatchReceiver = irGetFragment(scope)
            )
        }
    }

    /**
     * RumCall2Air a Rui function of this fragment.
     *
     * @param scope The function we call from.
     */
    fun buildRuiCallWithBridge(callee: IrSimpleFunction, scope: IrSimpleFunction, builder: IrBlockBodyBuilder) {
        builder.run {
            +irCallOp(
                callee.symbol,
                type = irBuiltIns.unitType,
                dispatchReceiver = irGetFragment(scope),
                argument = irGet(scope.valueParameters.first())
            )
        }
    }

    // ------------------------------------------------------------------------------
    // Patch
    // ------------------------------------------------------------------------------

    private fun IrBlockBodyBuilder.buildRuiPatch(symbolMap: RuiClassSymbols, function: IrSimpleFunction) {
        // SOURCE  val extendedScopeMask = ruiFragment.ruiExternalPatch(ruiFragment, scopeMask)
        val extendedScopeMask = irCallExternalPatch(function)

        // SOURCE  if (extendedScopeMask != 0L) fragment.ruiPatch(extendedScopeMask)
        +irIf(
            irNotEqual(
                irGet(extendedScopeMask),
                irConst(0L)
            ),
            irCallOp(
                symbolMap.patch.symbol,
                type = irBuiltIns.unitType,
                dispatchReceiver = irGetFragment(function),
                argument = irGet(extendedScopeMask)
            )
        )

        // SOURCE  ruiDirty0 = 0L
        //       rumClass.dirtyMasks.forEach { +it.builder.irClear(function.dispatchReceiverParameter!!) }
    }

    /**
     * RumCall2Air the external patch of the child fragment. This is somewhat complex because the function
     * is stored in a variable.
     *
     * ```kotlin
     * fragment.ruiExternalPatch(fragment, scopeMask)
     * ```
     *
     * ```text
     * CALL 'public abstract fun invoke (p1: P1 of kotlin.Function1): R of kotlin.Function1 [operator] declared in kotlin.Function1' type=kotlin.Unit origin=INVOKE
     *   $this: CALL 'public abstract fun <get-ruiExternalPatch> (): kotlin.Function1<@[ParameterName(name = 'it')] hu.simplexion.rui.runtime.RuiFragment<BT of hu.simplexion.rui.runtime.RuiFragment>, kotlin.Unit> declared in hu.simplexion.rui.runtime.RuiFragment' type=kotlin.Function1<@[ParameterName(name = 'it')] hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode>, kotlin.Unit> origin=GET_PROPERTY
     *     $this: CALL 'public open fun <get-fragment> (): hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> declared in hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air' type=hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> origin=GET_PROPERTY
     *       $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air declared in hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air.ruiPatch' type=hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air origin=null
     *   p1: CALL 'public open fun <get-fragment> (): hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> declared in hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air' type=hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> origin=GET_PROPERTY
     *     $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air declared in hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air.ruiPatch' type=hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air origin=null
     * ```
     */
    fun IrBlockBodyBuilder.irCallExternalPatch(function: IrSimpleFunction): IrVariable {

        val function2Type = irBuiltIns.functionN(2)
        val invoke = function2Type.functions.first { it.name.identifier == "invoke" }.symbol

        val fragment = irTemporary(irGetFragment(function))
//        val rootBuilder = rumClass.rootBlock.builder

        // returns with the extended scope mask
        return irTemporary(
            irCall(
                invoke,
                irBuiltIns.longType,
                valueArgumentsCount = 2,
                typeArgumentsCount = 0,
                origin = IrStatementOrigin.INVOKE
            ).apply {
//                dispatchReceiver = irCallOp(
//                    rootBuilder.symbolMap.externalPatchGetter.symbol,
//                    function2Type.defaultType,
//                    irGet(fragment)
//                )
                putValueArgument(0, irGet(fragment))
                putValueArgument(1, irGet(function.valueParameters[RUI_PATCH_ARGUMENT_INDEX_SCOPE_MASK]))
            }
        )
    }

    fun irTrace(point: String, parameters: List<IrExpression>): IrStatement {
        return irTrace(irGet(thisReceiver), point, parameters)
    }

    fun irTrace(function: IrFunction, point: String, parameters: List<IrExpression>): IrStatement {
        return irTrace(irGet(function.dispatchReceiverParameter!!), point, parameters)
    }

    fun irTrace(fragment: IrExpression, point: String, parameters: List<IrExpression>): IrStatement {
        return irTraceDirect(ruiClassBuilder.adapterPropertyBuilder.irGetValue(fragment), point, parameters)
    }

    /**
     * @param dispatchReceiver The `RuiAdapter` instance to use for the trace.
     */
    fun irTraceDirect(dispatchReceiver: IrExpression, point: String, parameters: List<IrExpression>): IrStatement {
        return IrCallImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            irBuiltIns.unitType,
            ruiContext.ruiAdapterTrace,
            typeArgumentsCount = 0,
            RUI_TRACE_ARGUMENT_COUNT,
        ).also {
            it.dispatchReceiver = dispatchReceiver
            it.putValueArgument(RUI_TRACE_ARGUMENT_NAME, irConst(rumClass.name.identifier))
            it.putValueArgument(RUI_TRACE_ARGUMENT_POINT, irConst(point))
            it.putValueArgument(RUI_TRACE_ARGUMENT_DATA, buildTraceVarArg(parameters))
        }
    }

    fun buildTraceVarArg(parameters: List<IrExpression>): IrExpression {
        return IrVarargImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            irBuiltIns.arrayClass.typeWith(irBuiltIns.anyNType),
            ruiContext.ruiFragmentType,
        ).also { vararg ->
            parameters.forEach {
                vararg.addElement(it)
            }
        }
    }
}