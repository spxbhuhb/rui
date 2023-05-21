/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RUI_PATCH_ARGUMENT_INDEX_SCOPE_MASK
import hu.simplexion.rui.kotlin.plugin.ir.RUI_STATE_VARIABLE_LIMIT
import hu.simplexion.rui.kotlin.plugin.ir.air.AirClass
import hu.simplexion.rui.kotlin.plugin.ir.air.AirStateVariable
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_RENDERING_VARIABLE
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_STATE_VARIABLE_SHADOW
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumExpression
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.psi.KtModifierListOwner

/**
 * Transforms state variable accesses from the original code into property
 * access in the generated IrClass.
 *
 * Adds:
 *
 * - `ruiInvalidate0(stateVariableIndex)` calls to invalidate the variable
 * - `ruiPatch(ruiDirty0)` calls to execute patch when necessary
 *
 * Applies to:
 *
 * - initializer statements
 * - function parameters
 */
class StateAccessTransform(
    private val irBuilder: ClassBoundIrBuilder,
    private val intoScope: IrValueSymbol
) : IrElementTransformerVoidWithContext(), RuiAnnotationBasedExtension {

    companion object {
        fun ClassBoundIrBuilder.transformStateAccess(expression: RumExpression, intoScope: IrValueSymbol): IrExpression =
            StateAccessTransform(this, intoScope)
                .visitExpression(
                    expression
                        .irExpression
                        .deepCopyWithVariables()
                )

        fun ClassBoundIrBuilder.transformStateAccess(statement: IrStatement, intoScope: IrValueSymbol): IrStatement =
            StateAccessTransform(this, intoScope)
                .visitElement(
                    statement.deepCopyWithVariables()
                ) as IrStatement
    }

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        irBuilder.context.annotations

    val airClass: AirClass
        get() = irBuilder.airClass

    val rumClass
        get() = airClass.rumClass

    val irContext = irBuilder.irContext

    val irBuiltIns = irContext.irBuiltIns

    fun scopeReceiver() = IrGetValueImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, intoScope)

    var haveToPatch = false

    override fun visitVariable(declaration: IrVariable): IrStatement {
        RUI_IR_RENDERING_VARIABLE.check(airClass.rumClass, declaration) {
            declaration.startOffset < airClass.rumClass.boundary.startOffset ||
                declaration.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR ||
                declaration.origin == IrDeclarationOrigin.FOR_LOOP_VARIABLE ||
                declaration.origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
        }

        return super.visitVariable(declaration)
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        haveToPatch = false
        val transformed = super.visitFunctionNew(declaration) as IrFunction
        if (!haveToPatch) return transformed

        val ps = parentScope // this is the IR scope, not the Rui scope

        return when {
            ps == null -> addPatch(transformed)
            ps.irElement is IrProperty -> TODO()
            ps.irElement is IrClass -> TODO()
            else -> transformed
        }
    }

    /**
     * Adds a call to the patch function as the last statement of the function body.
     */
    fun addPatch(function: IrFunction): IrFunction {
        val body = function.body ?: return function

        function.body = DeclarationIrBuilder(irContext, function.symbol).irBlockBody {
            for (statement in body.statements) +statement

            // FIXME this applies patch to the very end of the function, it should cover all returns
            // FIXME analyze all the possible uses of state changing functions and the possible scopes

            // SOURCE  patch(ruiDirty0)
            +irCall(
                airClass.patch.symbol,
                irBuiltIns.unitType,
                valueArgumentsCount = 1,
                typeArgumentsCount = 0,
                origin = IrStatementOrigin.INVOKE
            ).apply {
                dispatchReceiver = scopeReceiver()

                putValueArgument(
                    RUI_PATCH_ARGUMENT_INDEX_SCOPE_MASK,
                    irBuilder.irGetValue(airClass.dirtyMasks.first().irProperty, scopeReceiver())
                )
            }
        }

        return function
    }

    override fun visitGetValue(expression: IrGetValue): IrExpression {

        val name = expression.symbol.owner.name.identifier

        val stateVariable = airClass.stateVariableMap[name]
            ?: return super.visitGetValue(expression)

        RUI_IR_STATE_VARIABLE_SHADOW.check(rumClass, expression) { stateVariable.rumElement.matches(expression.symbol) }

        val property = airClass.stateVariableMap[name]?.irProperty
            ?: throw IllegalStateException("missing state variable $name in ${rumClass.originalFunction.name}")

        return irBuilder.irGetValue(property, scopeReceiver())
    }

    /**
     * Replaces local variable change with class property change.
     *
     * Replaces only top level function variables. Others (one defined in a block)
     * are not reactive, and should not be replaced.
     */
    override fun visitSetValue(expression: IrSetValue): IrExpression {

        val name = expression.symbol.owner.name.identifier

        val stateVariable = airClass.stateVariableMap[name]
            ?: return super.visitSetValue(expression)

        RUI_IR_STATE_VARIABLE_SHADOW.check(rumClass, expression) { stateVariable.rumElement.matches(expression.symbol) }

        if (currentScope == null) return super.visitSetValue(expression)

        return DeclarationIrBuilder(irContext, currentScope!!.scope.scopeOwnerSymbol).irComposite {

            val traceData = traceStateChangeBefore(stateVariable)

            +irBuilder.irSetValue(stateVariable.irProperty, expression.value, scopeReceiver())

            val index = stateVariable.rumElement.index
            val dirtyMask = airClass.dirtyMasks[index / RUI_STATE_VARIABLE_LIMIT]

            +irCallOp(
                dirtyMask.invalidate.symbol,
                irBuiltIns.unitType,
                IrGetValueImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, intoScope),
                (index % RUI_STATE_VARIABLE_LIMIT).toIrConst(irBuiltIns.longType)
            )

            traceStateChangeAfter(stateVariable, traceData)
        }
    }

    fun IrBlockBuilder.traceStateChangeBefore(stateVariable: AirStateVariable): IrVariable? {
        if (!irBuilder.context.withTrace) return null

        return irTemporary(irTraceGet(stateVariable, irBuilder.irThisReceiver()))
            .also { it.parent = currentFunction!!.irElement as IrFunction }
    }

    fun IrBlockBuilder.traceStateChangeAfter(stateVariable: AirStateVariable, traceData: IrVariable?) {
        if (traceData == null) return

        irBuilder.irTrace("state change", listOf(
            irString("${stateVariable.irProperty.name.identifier}:"),
            irGet(traceData),
            irString(" ⇢ "),
            irTraceGet(stateVariable, irBuilder.irThisReceiver())
        ))
    }

    fun IrBlockBuilder.irTraceGet(stateVariable: AirStateVariable, receiver: IrExpression): IrExpression =
        stateVariable.irProperty.backingField
            ?.let { irGetField(receiver, it) }
            ?: irString("?")
}
