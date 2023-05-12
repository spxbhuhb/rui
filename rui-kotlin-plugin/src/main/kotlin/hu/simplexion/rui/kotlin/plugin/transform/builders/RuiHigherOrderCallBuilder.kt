/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.builders

import hu.simplexion.rui.kotlin.plugin.*
import hu.simplexion.rui.kotlin.plugin.model.RuiExpression
import hu.simplexion.rui.kotlin.plugin.model.RuiHigherOrderCall
import hu.simplexion.rui.kotlin.plugin.transform.RuiClassSymbols
import hu.simplexion.rui.kotlin.plugin.transform.util.RuiScopeTransform
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.Name

class RuiHigherOrderCallBuilder(
    override val ruiClassBuilder: RuiClassBuilder,
    val ruiHigherOrderCall: RuiHigherOrderCall
) : RuiFragmentBuilder {

    var callSiteDependencyMask = 0L

    // we have to initialize this in build, after all other classes in the module are registered
    override lateinit var symbolMap: RuiClassSymbols

    private lateinit var externalPatch: IrSimpleFunction
    private lateinit var dispatchReceiver: IrValueParameter

    override fun buildDeclarations() {
        calcCallSiteDependencyMask()

        tryBuild(ruiHigherOrderCall.irCall) {
            symbolMap = ruiContext.ruiSymbolMap.getSymbolMap(ruiHigherOrderCall.targetRuiClass)
            buildExternalPatch()
        }
    }

    private fun calcCallSiteDependencyMask() {
        callSiteDependencyMask = 0
        for (argument in ruiHigherOrderCall.valueArguments) {
            for (stateVariable in argument.dependencies) {
                callSiteDependencyMask = callSiteDependencyMask or (1L shl stateVariable.index)
            }
        }
    }

    private fun buildExternalPatch() {
        irFactory.buildFun {
            name = Name.identifier("$RUI_EXTERNAL_PATCH_OF_CHILD${ruiHigherOrderCall.irCall.startOffset}")
            returnType = irBuiltIns.longType
            modality = Modality.OPEN
        }.also { function ->

            externalPatch = function
            function.parent = irClass

            dispatchReceiver = function.addDispatchReceiver {
                type = irClass.typeWith(irClass.typeParameters.first().defaultType)
            }

            val externalPatchIt = function.addValueParameter {
                name = Name.identifier("it")
                type = classBoundFragmentType
            }

            val scopeMask = function.addValueParameter {
                name = Name.identifier("scopeMask")
                type = irBuiltIns.longType
            }

            function.body = irExternalPatchBody(function, externalPatchIt, scopeMask)

            irClass.declarations += function
        }
    }

    private fun irExternalPatchBody(function: IrSimpleFunction, externalPatchIt: IrValueParameter, scopeMask: IrValueParameter): IrBody =
        DeclarationIrBuilder(irContext, function.symbol).irBlockBody {
            traceExternalPatch()

            + irIf(
                irEqual(
                    irAnd(irGet(scopeMask), irConst(callSiteDependencyMask)),
                    irConst(0)
                ),
                irReturn(
                    irGet(scopeMask)
                )
            )

            + irAs(symbolMap.defaultType, irGet(externalPatchIt))

            ruiHigherOrderCall.valueArguments.forEachIndexed { index, ruiExpression ->
                irVariablePatch(externalPatchIt, index, ruiExpression)
            }

            + irReturn(
                irGet(scopeMask)
            )
        }

    private fun IrBlockBodyBuilder.traceExternalPatch() {

        if (! ruiContext.withTrace) return

        val args = mutableListOf<IrExpression>()

        ruiClass.dirtyMasks.forEach {
            args += irString("${it.name}:")
            args += it.builder.propertyBuilder.irGetValue(irGet(dispatchReceiver))
        }

        ruiClassBuilder.irTrace(irGet(dispatchReceiver), "external patch", args)

    }

    private fun IrBlockBodyBuilder.irVariablePatch(externalPatchIt: IrValueDeclaration, index: Int, ruiExpression: RuiExpression) {
        // constants, globals, etc. have no dependencies, no need to patch them
        if (ruiExpression.dependencies.isEmpty()) return

        // lambdas and anonymous functions cannot change and cannot be patched
        // FIXME check if lambda and anonymous func results in IrFunctionExpression
        if (ruiExpression.irExpression is IrFunctionExpression) return

        + irIf(
            irCondition(ruiExpression),
            irPatchResult(externalPatchIt, index, ruiExpression)
        )
    }

    private fun irCondition(ruiExpression: RuiExpression): IrExpression {
        val dependencies = ruiExpression.dependencies
        var result = dependencies[0].builder.irIsDirty(irGet(dispatchReceiver))
        for (i in 1 until dependencies.size) {
            result = irOrOr(result, dependencies[i].builder.irIsDirty(irGet(dispatchReceiver)))
        }
        return result
    }

    private fun IrBlockBodyBuilder.irPatchResult(externalPatchIt: IrValueDeclaration, index: Int, ruiExpression: RuiExpression): IrExpression {
        return irBlock {
            val traceData = traceStateChangeBefore(externalPatchIt, index)

            val newValue = irTemporary(
                RuiScopeTransform(ruiClassBuilder.ruiClass, dispatchReceiver.symbol)
                    .visitExpression(
                        ruiExpression.irExpression.deepCopyWithVariables()
                    )
            )

            // set the state variable in the child fragment
            + irCall(
                symbolMap.setterFor(index),
                origin = null,
                dispatchReceiver = irImplicitAs(symbolMap.defaultType, irGet(externalPatchIt)),
                extensionReceiver = null,
                irGet(newValue)
            )

            // call invalidate of the child fragment
            + irCall(
                symbolMap.getInvalidate(index / 32), null,
                irImplicitAs(symbolMap.defaultType, irGet(externalPatchIt)), null,
                irConst(1 shl (index % 32))
            )

            traceStateChangeAfter(index, traceData, newValue)
        }
    }

    private fun IrBlockBuilder.traceStateChangeBefore(externalPatchIt: IrValueDeclaration, index: Int): IrVariable? {

        if (! ruiContext.withTrace) return null

        return irTemporary(irTraceGet(index, irImplicitAs(symbolMap.defaultType, irGet(externalPatchIt))))
    }

    private fun IrBlockBuilder.traceStateChangeAfter(index: Int, traceData: IrVariable?, newValue: IrVariable) {
        if (traceData == null) return

        val property = symbolMap.getStateVariable(index).property

        ruiClassBuilder.irTrace(
            "state change", listOf(
                irString("${property.name}:"),
                irGet(traceData),
                irString(" ⇢ "),
                irGet(newValue)
            )
        )
    }

    override fun irNewInstance(): IrExpression =
        IrConstructorCallImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            symbolMap.defaultType,
            symbolMap.primaryConstructor.symbol,
            typeArgumentsCount = 1, // bridge type
            constructorTypeArgumentsCount = 0,
            ruiHigherOrderCall.valueArguments.size + RUI_FRAGMENT_ARGUMENT_COUNT // +3 = adapter + scope + external patch
        ).also { constructorCall ->

            constructorCall.putTypeArgument(RUI_FRAGMENT_TYPE_INDEX_BRIDGE, classBoundBridgeType.defaultType)

            constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER, ruiClassBuilder.adapterPropertyBuilder.irGetValue())
            constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_SCOPE, ruiClassBuilder.scopePropertyBuilder.irGetValue())
            constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_EXTERNAL_PATCH, irExternalPatchReference())

            ruiHigherOrderCall.valueArguments.forEachIndexed { index, ruiExpression ->
                constructorCall.putValueArgument(index + RUI_FRAGMENT_ARGUMENT_COUNT, ruiExpression.irExpression)
            }
        }

    fun irExternalPatchReference(): IrExpression =
        IrFunctionReferenceImpl.fromSymbolOwner(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            classBoundExternalPatchType,
            externalPatch.symbol,
            typeArgumentsCount = 0,
            reflectionTarget = externalPatch.symbol
        ).also {
            it.dispatchReceiver = irThisReceiver()
        }

}