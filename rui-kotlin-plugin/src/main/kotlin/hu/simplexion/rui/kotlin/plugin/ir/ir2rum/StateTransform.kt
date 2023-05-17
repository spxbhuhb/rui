/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.ir2rum

import hu.simplexion.rui.kotlin.plugin.ir.RUI_PATCH_ARGUMENT_INDEX_SCOPE_MASK
import hu.simplexion.rui.kotlin.plugin.ir.RUI_STATE_VARIABLE_LIMIT
import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_RENDERING_VARIABLE
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_STATE_VARIABLE_SHADOW
import hu.simplexion.rui.kotlin.plugin.ir.rum.*
import hu.simplexion.rui.kotlin.plugin.ir.util.*
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.psi.KtModifierListOwner
import kotlin.collections.set

/**
 * Transforms state variable accesses from the original code into property
 * access in the generated IrClass.
 *
 * The [blockStack] is used to keep track of variable declarations. Only top
 * level variables are converted into state variables. Values (top lever or not)
 * and not top level variables are kept as they are.
 *
 * Without this we can't make difference between `bb` variables when we
 * reach the `println`.
 *
 * ```
 * @Rui
 * fun Text() {
 *     var bb = 0
 *     if (bb == 0) {
 *        var bb = "hello"
 *        println(bb)
 *     }
 * }
 * ```
 *
 * The bottom of the stack is the top level block, the component itself.
 * As only variables declared at the top level block are moved into state
 * variables, we can check the stack if the given variable is a local
 * one or not.
 *
 * @property  skipParameters  Skip the first N parameter of the original function when
 *                            converting the function parameters into external state variables.
 *                            Used for entry points when the first parameter is the adapter.
 */
class StateTransform(
    private val ruiContext: RuiPluginContext,
    private val rumClass: RumClass,
    private val skipParameters: Int
) : IrElementTransformerVoidWithContext(), RuiAnnotationBasedExtension {

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        ruiContext.annotations

    val irContext = ruiContext.irContext

    val irBuiltIns = irContext.irBuiltIns

    var currentStatementIndex = 0

    var stateVariableIndex = 0
        get() = field++

    val blockStack: Stack<BlockStackEntry> = mutableListOf(BlockStackEntry(true))

    var lastPop: BlockStackEntry = blockStack[0]

    /**
     * @property  top                   True means first block, the original function itself.
     * @property  stateVariableChange   True means that this block does change state variables.
     * @property  functionOrLambda      True means that this block is part of a function or lambda declaration.
     * @property  variables             Names of variables declared in this block.
     */
    class BlockStackEntry(
        val top: Boolean = false,
        var stateVariableChange: Boolean = false,
        var functionOrLambda: Boolean = false,
        val variables: MutableList<String> = mutableListOf()
    )

    fun String.isStateVariable(): Boolean {
        for (i in blockStack.indices.reversed()) {
            blockStack[i].also {
                if (this in it.variables) return it.top
            }
        }
        // TODO check all the cases variables may be accessed
        return false // this is some global variable
    }

    fun transform() {

        rumClass.originalFunction.valueParameters.forEachIndexed { index, valueParameter ->

            if (index < skipParameters) return@forEachIndexed

            RumExternalStateVariable(rumClass, stateVariableIndex, valueParameter).also {
                register(it, valueParameter)
                addDirtyMask(it)
            }
        }

        rumClass.originalStatements.forEachIndexed { index, irStatement ->

            currentStatementIndex = index

            if (irStatement is IrVariable) {
                transformStateVariable(irStatement)
            } else {
                transformNotVariable(irStatement)
            }
        }
    }

    fun transformStateVariable(irStatement: IrVariable) {
        if (currentStatementIndex >= rumClass.boundary) {
            RUI_IR_RENDERING_VARIABLE.report(rumClass, irStatement)
            return
        }

        RumInternalStateVariable(rumClass, stateVariableIndex, irStatement).also {
            register(it, irStatement)
            addDirtyMask(it)
        }
    }

    fun transformNotVariable(irStatement: IrStatement) {
        if (currentStatementIndex < rumClass.boundary) {
            rumClass.initializerStatements += irStatement.transform(this, null) as IrStatement
        } else {
            rumClass.renderingStatements += irStatement.transform(this, null) as IrStatement
        }
    }

    fun register(it: RumStateVariable, declaration: IrDeclaration) {
        // variable shadowing is a bad practice anyway, no big loss to forbid it
        if (it.originalName in rumClass.stateVariables) {
            RUI_IR_STATE_VARIABLE_SHADOW.report(rumClass, declaration)
            return
        }

        val root = blockStack[0]
        rumClass.stateVariables[it.originalName] = it
        root.variables += it.originalName
        rumClass.symbolMap[it.builder.getter.symbol] = it
    }

    fun addDirtyMask(it: RumStateVariable) {
        val maskNumber = it.index / 32
        if (rumClass.dirtyMasks.size > maskNumber) return
        rumClass.dirtyMasks += RumDirtyMask(rumClass, maskNumber)
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        blockStack.peek().variables += declaration.name.identifier

        RUI_IR_RENDERING_VARIABLE.check(rumClass, declaration) {
            currentStatementIndex < rumClass.boundary ||
                declaration.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR ||
                declaration.origin == IrDeclarationOrigin.FOR_LOOP_VARIABLE ||
                declaration.origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
        }

        return super.visitVariable(declaration)
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        val transformed = super.visitFunctionNew(declaration) as IrFunction

        if (!lastPop.stateVariableChange) return transformed

        val ps = parentScope // this is the IR scope, not the Rui scope

        return when {
            ps == null -> irPatch(transformed)
            ps.irElement is IrProperty -> TODO()
            ps.irElement is IrClass -> TODO()
            else -> transformed
        }
    }

    /**
     * Adds a call to the patch function as the last statement of the function body.
     */
    fun irPatch(function: IrFunction): IrFunction {
        val body = function.body ?: return function

        function.body = DeclarationIrBuilder(irContext, function.symbol).irBlockBody {
            for (statement in body.statements) +statement

            // SOURCE  this.patch(ruiDirty0)
            +irCall(
                rumClass.builder.patch.symbol,
                rumClass.builder.irBuiltIns.unitType,
                valueArgumentsCount = 1,
                typeArgumentsCount = 0,
                origin = IrStatementOrigin.INVOKE
            ).apply {
                dispatchReceiver = rumClass.builder.irThisReceiver()

                putValueArgument(
                    RUI_PATCH_ARGUMENT_INDEX_SCOPE_MASK,
                    rumClass.dirtyMasks.first().builder.propertyBuilder.irGetValue(dispatchReceiver!!)
                )
            }
        }

        return function
    }

    override fun visitBlock(expression: IrBlock): IrExpression {
        blockStack.push(BlockStackEntry())
        val result = super.visitBlock(expression)
        lastPop = blockStack.pop()
        return result
    }

    override fun visitBlockBody(body: IrBlockBody): IrBody {
        blockStack.push(BlockStackEntry())
        val result = super.visitBlockBody(body)
        lastPop = blockStack.pop()
        return result
    }

    override fun visitGetValue(expression: IrGetValue): IrExpression {

        val name = expression.symbol.owner.name.identifier

        if (!name.isStateVariable()) return super.visitGetValue(expression)

        return rumClass.stateVariables[name]
            ?.builder?.irGetValue()
            ?: throw IllegalStateException("missing state variable $name in ${rumClass.originalFunction.name}")
    }

    /**
     * Replaces local variable change with class property change.
     *
     * Replaces only top level function variables. Others (one defined in a block)
     * are not reactive, and should not be replaced.
     */
    override fun visitSetValue(expression: IrSetValue): IrExpression {

        val name = expression.symbol.owner.name.identifier

        if (!name.isStateVariable()) return super.visitSetValue(expression)

        if (currentScope == null) return super.visitSetValue(expression)

        // Sets all but the top entries in the block stack as state variable
        // changing block.

        var idx = blockStack.lastIndex
        while (idx > 0) {
            blockStack[idx].stateVariableChange = true
            idx--
        }

        return DeclarationIrBuilder(irContext, currentScope!!.scope.scopeOwnerSymbol).irComposite {
            val stateVariable = rumClass.stateVariables[name]
                ?: throw IllegalStateException("missing state variable $name in ${rumClass.originalFunction.name}")

            val traceData = traceStateChangeBefore(stateVariable)

            +stateVariable.builder.irSetValue(expression.value)

            +irCallOp(
                rumClass.dirtyMasks[stateVariable.index / RUI_STATE_VARIABLE_LIMIT].builder.invalidate,
                irBuiltIns.unitType,
                rumClass.builder.irThisReceiver(),
                (stateVariableIndex % RUI_STATE_VARIABLE_LIMIT).toIrConst(irBuiltIns.longType)
            )

            traceStateChangeAfter(stateVariable, traceData)
        }
    }

    fun IrBlockBuilder.traceStateChangeBefore(stateVariable: RumStateVariable): IrVariable? {
        if (!ruiContext.withTrace) return null

        return irTemporary(irTraceGet(stateVariable, rumClass.builder.irThisReceiver()))
            .also { it.parent = currentFunction!!.irElement as IrFunction }
    }

    fun IrBlockBuilder.traceStateChangeAfter(stateVariable: RumStateVariable, traceData: IrVariable?) {
        if (traceData == null) return

        rumClass.builder.irTrace("state change", listOf(
            irString("${stateVariable.name}:"),
            irGet(traceData),
            irString(" ⇢ "),
            irTraceGet(stateVariable, rumClass.builder.irThisReceiver())
        ))
    }

    fun IrBlockBuilder.irTraceGet(stateVariable: RumStateVariable, receiver: IrExpression): IrExpression =
        stateVariable.builder.irProperty.backingField
            ?.let { irGetField(receiver, it) }
            ?: irString("?")
}
