package hu.simplexion.rui.kotlin.plugin.transform.builders

import hu.simplexion.rui.kotlin.plugin.RUI_EXTERNAL_PATCH_OF_CHILD
import hu.simplexion.rui.kotlin.plugin.model.RuiExpression
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
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.Name

/**
 * Generates external patch functions.
 *
 * External patch generation happens in:
 *
 * - [RuiCallBuilder]
 * - [RuiHigherOrderArgumentBuilder.buildImplicitComponentExternalPatch]
 *
 * The generated code looks like this (NNN = call site offset, CCC = child component name):
 *
 * ```kotlin
 *     fun ruiEpNNN(it: RuiFragment<BT>, scopeMask: Long): Long { // buildExternalPatch
 *         ruiAdapter.trace("<component-name>", "ruiEpNNN", "scopeMask", scopeMask, "callSiteDependencies", callSiteDependencies)   // traceExternalPatch
 *         if (scopeMask and callSiteDependency != 0L) return 0L  // irExternalPatchBody
 *
 *         it as RuiCCC // irExternalPatchBody
 *         if (ruiDirty0 and 1L != 0L) { // irVariablePatchCondition
 *             it.p0 = i   // irVariableSetAndInvalidate
 *             it.ruiInvalidate0(1L) // irVariableSetAndInvalidate
 *         }
 *
 *         return 0L // irExternalPatchBody
 *     }
 * ```
 */
class RuiExternalPatchBuilder(
    override val ruiClassBuilder: RuiClassBuilder,
    val callSiteOffset: Int,
    val arguments: List<RuiExpression>,
    val callSiteDependencyMask: Long,
    override val symbolMap: RuiClassSymbols
) : RuiBuilderWithSymbolMap {

    lateinit var externalPatch: IrSimpleFunction
    private lateinit var dispatchReceiver: IrValueParameter

    fun buildExternalPatch() {
        irFactory.buildFun {
            name = Name.identifier("$RUI_EXTERNAL_PATCH_OF_CHILD$callSiteOffset")
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

            +irIf(
                irEqual(
                    irAnd(irGet(scopeMask), irConst(callSiteDependencyMask)),
                    irConst(0)
                ),
                irReturn(
                    irGet(scopeMask)
                )
            )

            +irAs(symbolMap.defaultType, irGet(externalPatchIt))

            arguments.forEachIndexed { index, ruiExpression ->
                irVariablePatch(externalPatchIt, index, ruiExpression)
            }

            +irReturn(
                irGet(scopeMask)
            )
        }

    private fun IrBlockBodyBuilder.traceExternalPatch() {

        if (!ruiContext.withTrace) return

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

        +irIf(
            irVariablePatchCondition(ruiExpression),
            irVariableSetAndInvalidate(externalPatchIt, index, ruiExpression)
        )
    }

    private fun irVariablePatchCondition(ruiExpression: RuiExpression): IrExpression {
        val dependencies = ruiExpression.dependencies
        var result = dependencies[0].builder.irIsDirty(irGet(dispatchReceiver))
        for (i in 1 until dependencies.size) {
            result = irOrOr(result, dependencies[i].builder.irIsDirty(irGet(dispatchReceiver)))
        }
        return result
    }

    private fun IrBlockBodyBuilder.irVariableSetAndInvalidate(externalPatchIt: IrValueDeclaration, index: Int, ruiExpression: RuiExpression): IrExpression {
        return irBlock {
            val traceData = traceStateChangeBefore(externalPatchIt, index)

            val newValue = irTemporary(
                RuiScopeTransform(ruiClassBuilder.ruiClass, dispatchReceiver.symbol)
                    .visitExpression(
                        ruiExpression.irExpression.deepCopyWithVariables()
                    )
            )

            // set the state variable in the child fragment
            +irCall(
                symbolMap.setterFor(index),
                origin = null,
                dispatchReceiver = irImplicitAs(symbolMap.defaultType, irGet(externalPatchIt)),
                extensionReceiver = null,
                irGet(newValue)
            )

            // call invalidate of the child fragment
            +irCall(
                symbolMap.getInvalidate(index / 32), null,
                irImplicitAs(symbolMap.defaultType, irGet(externalPatchIt)), null,
                irConst(1 shl (index % 32))
            )

            traceStateChangeAfter(index, traceData, newValue)
        }
    }

    private fun IrBlockBuilder.traceStateChangeBefore(externalPatchIt: IrValueDeclaration, index: Int): IrVariable? {

        if (!ruiContext.withTrace) return null

        return irTemporary(irTraceGet(index, irImplicitAs(symbolMap.defaultType, irGet(externalPatchIt))))
    }

    private fun IrBlockBuilder.traceStateChangeAfter(index: Int, traceData: IrVariable?, newValue: IrVariable) {
        if (traceData == null) return

        val property = symbolMap.getStateVariable(index).property

        ruiClassBuilder.irTrace(
            "state change",
            listOf(
                irString("${property.name}:"),
                irGet(traceData),
                irString(" ⇢ "),
                irGet(newValue)
            )
        )
    }

    fun irExternalPatchReference(): IrExpression =
        IrFunctionReferenceImpl.fromSymbolOwner(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            classBoundExternalPatchType,
            externalPatch.symbol,
            typeArgumentsCount = 0,
            reflectionTarget = externalPatch.symbol
        ).also {
            it.dispatchReceiver = irThisReceiver() // TODO this receiver cleanup (should be properly scoped)
        }

}