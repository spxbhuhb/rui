package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RUI_EXTERNAL_PATCH_ARGUMENT_INDEX_FRAGMENT
import hu.simplexion.rui.kotlin.plugin.ir.RUI_EXTERNAL_PATCH_ARGUMENT_INDEX_SCOPE_MASK
import hu.simplexion.rui.kotlin.plugin.ir.RUI_STATE_VARIABLE_LIMIT
import hu.simplexion.rui.kotlin.plugin.ir.air.AirExternalPatchCall
import hu.simplexion.rui.kotlin.plugin.ir.air.AirStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.air2ir.StateAccessTransform.Companion.transformStateAccess
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumExpression
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression

/**
 * Generates external patch for calls.
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
class AirExternalPatchCall2Ir(
    parent: ClassBoundIrBuilder,
    val externalPatch: AirExternalPatchCall
) : ClassBoundIrBuilder(parent) {

    val rumCall
        get() = externalPatch.rumElement

    val arguments
        get() = rumCall.valueArguments

    val dispatchReceiver
        get() = externalPatch.irFunction.dispatchReceiverParameter!!

    val symbolMap = rumCall.symbolMap(this)

    val callSiteDependencyMask = calcCallSiteDependencyMask()

    fun toIr() {
        val function = externalPatch.irFunction

        val externalPatchIt = function.valueParameters[RUI_EXTERNAL_PATCH_ARGUMENT_INDEX_FRAGMENT]
        val scopeMask = function.valueParameters[RUI_EXTERNAL_PATCH_ARGUMENT_INDEX_SCOPE_MASK]

        function.body = DeclarationIrBuilder(irContext, function.symbol).irBlockBody {

            +irIf(
                irEqual(
                    irAnd(irGet(scopeMask), irConst(callSiteDependencyMask)),
                    irConst(0)
                ),
                irReturn(
                    irGet(scopeMask)
                )
            )

            +irAs(irGet(externalPatchIt), symbolMap.defaultType)

            arguments.forEachIndexed { index, ruiExpression ->
                irVariablePatch(externalPatchIt, index, ruiExpression)
            }

            +irReturn(
                irGet(scopeMask)
            )
        }
    }

    private fun calcCallSiteDependencyMask(): Long {
        var mask = 0L
        for (argument in arguments) {
            for (index in argument.dependencies) {
                mask = mask or (1L shl index)
            }
        }
        return mask
    }

    private fun IrBlockBodyBuilder.irVariablePatch(externalPatchIt: IrValueDeclaration, index: Int, expression: RumExpression) {
        // constants, globals, etc. have no dependencies, no need to patch them
        if (expression.dependencies.isEmpty()) return

        // lambdas and anonymous functions cannot change and cannot be patched
        // FIXME check if lambda and anonymous func results in IrFunctionExpression
        if (expression.irExpression is IrFunctionExpression) return

        +irIf(
            irVariablePatchCondition(expression),
            irVariableSetAndInvalidate(externalPatchIt, index, expression)
        )
    }

    private fun irVariablePatchCondition(expression: RumExpression): IrExpression {
        val dependencies = expression.dependencies

        var stateVariable = airClass.stateVariableList[dependencies[0]]
        var result = stateVariable.irIsDirty(irGet(dispatchReceiver))

        for (i in 1 until dependencies.size) {
            stateVariable = airClass.stateVariableList[dependencies[i]]
            result = irOrOr(result, stateVariable.irIsDirty(irGet(dispatchReceiver)))
        }
        return result
    }

    fun AirStateVariable.irIsDirty(receiver: IrExpression): IrExpression {
        val variableIndex = rumElement.index
        val maskIndex = variableIndex / RUI_STATE_VARIABLE_LIMIT
        val bitIndex = variableIndex % RUI_STATE_VARIABLE_LIMIT

        val mask = airClass.dirtyMasks[maskIndex]

        return irNotEqual(
            irAnd(irGetValue(mask.irProperty, receiver), irConst(1L shl bitIndex)),
            irConst(0L)
        )
    }

    private fun IrBlockBodyBuilder.irVariableSetAndInvalidate(externalPatchIt: IrValueDeclaration, index: Int, ruiExpression: RumExpression): IrExpression {
        return irBlock {

            val newValue = irTemporary(
                transformStateAccess(ruiExpression, dispatchReceiver.symbol)
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
                symbolMap.getInvalidate(index / RUI_STATE_VARIABLE_LIMIT), null,
                irImplicitAs(symbolMap.defaultType, irGet(externalPatchIt)), null,
                irConst(1L shl (index % RUI_STATE_VARIABLE_LIMIT))
            )

        }
    }
}
