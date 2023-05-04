/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.fromir

import hu.simplexion.rui.kotlin.plugin.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui.RUI_IR_RENDERING_VARIABLE
import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui.RUI_IR_STATE_VARIABLE_SHADOW
import hu.simplexion.rui.kotlin.plugin.model.*
import hu.simplexion.rui.kotlin.plugin.util.*
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
class RuiStateTransform(
    private val ruiContext: RuiPluginContext,
    private val ruiClass: RuiClass,
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

        ruiClass.irFunction.valueParameters.forEachIndexed { index, valueParameter ->

            if (index < skipParameters) return@forEachIndexed

            RuiExternalStateVariable(ruiClass, stateVariableIndex, valueParameter).also {
                register(it, valueParameter)
                addDirtyMask(it)
            }
        }

        ruiClass.originalStatements.forEachIndexed { index, irStatement ->

            currentStatementIndex = index

            if (irStatement is IrVariable) {
                transformStateVariable(irStatement)
            } else {
                transformNotVariable(irStatement)
            }
        }
    }

    fun transformStateVariable(irStatement: IrVariable) {
        if (currentStatementIndex >= ruiClass.boundary) {
            RUI_IR_RENDERING_VARIABLE.report(ruiClass, irStatement)
            return
        }

        RuiInternalStateVariable(ruiClass, stateVariableIndex, irStatement).also {
            register(it, irStatement)
            addDirtyMask(it)
        }
    }

    fun transformNotVariable(irStatement: IrStatement) {
        if (currentStatementIndex < ruiClass.boundary) {
            ruiClass.initializerStatements += irStatement.transform(this, null) as IrStatement
        } else {
            ruiClass.renderingStatements += irStatement.transform(this, null) as IrStatement
        }
    }

    fun register(it: RuiStateVariable, declaration: IrDeclaration) {
        // variable shadowing is a bad practice anyway, no big loss to forbid it
        if (it.originalName in ruiClass.stateVariables) {
            RUI_IR_STATE_VARIABLE_SHADOW.report(ruiClass, declaration)
            return
        }

        val root = blockStack[0]
        ruiClass.stateVariables[it.originalName] = it
        root.variables += it.originalName
        ruiClass.symbolMap[it.builder.getter.symbol] = it
    }

    fun addDirtyMask(it: RuiStateVariable) {
        val maskNumber = it.index / 32
        if (ruiClass.dirtyMasks.size > maskNumber) return
        ruiClass.dirtyMasks += RuiDirtyMask(ruiClass, maskNumber)
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        blockStack.peek().variables += declaration.name.identifier

        RUI_IR_RENDERING_VARIABLE.check(ruiClass, declaration) {
            currentStatementIndex < ruiClass.boundary ||
                declaration.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR ||
                declaration.origin == IrDeclarationOrigin.FOR_LOOP_VARIABLE ||
                declaration.origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
        }

        return super.visitVariable(declaration)
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        val transformed = super.visitFunctionNew(declaration) as IrFunction

        if (!lastPop.stateVariableChange) return transformed

        val ps = parentScope

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
            +irCallOp(
                ruiClass.builder.patch.symbol,
                ruiClass.builder.irBuiltIns.unitType,
                ruiClass.builder.irThisReceiver()
            )
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

        return ruiClass.stateVariables[name]
            ?.builder?.irGetValue()
            ?: throw IllegalStateException("missing state variable $name in ${ruiClass.irFunction.name}")
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
            val stateVariable = ruiClass.stateVariables[name] ?: throw IllegalStateException("missing state variable $name in ${ruiClass.irFunction.name}")

            val traceData = traceStateChangeBefore(stateVariable)

            +stateVariable.builder.irSetValue(expression.value)

            +irCallOp(
                ruiClass.dirtyMasks[stateVariable.index / 32].builder.invalidate,
                irBuiltIns.unitType,
                ruiClass.builder.irThisReceiver(),
                (stateVariableIndex % 32).toIrConst(irBuiltIns.intType)
            )

            traceStateChangeAfter(stateVariable, traceData)
        }
    }

    // eventHandler: FUN_EXPR type=kotlin.Function1<@[ParameterName(name = 'np0')] kotlin.Int, kotlin.Unit> origin=LAMBDA
    //                FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> (it:kotlin.Int) returnType:kotlin.Unit
    //                  VALUE_PARAMETER name:it index:0 type:kotlin.Int
    //                  BLOCK_BODY
    //                    SET_VAR 'var i: kotlin.Int [var] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment' type=kotlin.Unit origin=EQ
    //                      GET_VAR 'it: kotlin.Int declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment.<anonymous>' type=kotlin.Int origin=null


    // eventHandler: FUN_EXPR type=kotlin.Function1<@[ParameterName(name = 'np0')] kotlin.Int, kotlin.Unit> origin=LAMBDA
    //                    FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> (it:kotlin.Int) returnType:kotlin.Unit
    //                      VALUE_PARAMETER name:it index:0 type:kotlin.Int
    //                      BLOCK_BODY
    //                        COMPOSITE type=kotlin.Unit origin=null
    //                          VAR IR_TEMPORARY_VARIABLE name:tmp0 type:kotlin.Int [val]
    //                            GET_FIELD 'FIELD PROPERTY_BACKING_FIELD name:i type:kotlin.Int visibility:private' type=kotlin.Int origin=null
    //                              receiver: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                          CALL 'public final fun set-i (set-?: kotlin.Int): kotlin.Unit declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=kotlin.Int origin=null
    //                            $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                            set-?: GET_VAR 'it: kotlin.Int declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment.<anonymous>' type=kotlin.Int origin=null
    //                          CALL 'public open fun ruiInvalidate0 (mask: kotlin.Int): kotlin.Unit declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=kotlin.Unit origin=null
    //                            $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                            mask: CONST Int type=kotlin.Int value=1
    //                          CALL 'public final fun println (message: kotlin.Any?): kotlin.Unit [inline] declared in kotlin.io.ConsoleKt' type=kotlin.Unit origin=null
    //                            message: STRING_CONCATENATION type=kotlin.String
    //                              CONST String type=kotlin.String value="[RuiEventHandlerFragment       ]  state change          |"
    //                              CONST String type=kotlin.String value=" i: "
    //                              GET_VAR 'val tmp0: kotlin.Int [val] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment.<anonymous>' type=kotlin.Int origin=null
    //                              CONST String type=kotlin.String value=" ⇢ "
    //                              GET_FIELD 'FIELD PROPERTY_BACKING_FIELD name:i type:kotlin.Int visibility:private' type=kotlin.Int origin=null
    //                                receiver: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                        CALL 'public open fun ruiPatch (): kotlin.Unit declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=kotlin.Unit origin=null
    //                          $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null


    // eventHandler: FUN_EXPR type=kotlin.Function1<@[ParameterName(name = 'np0')] kotlin.Int, kotlin.Unit> origin=LAMBDA
    //                FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> (it:kotlin.Int) returnType:kotlin.Unit
    //                  VALUE_PARAMETER name:it index:0 type:kotlin.Int
    //                  BLOCK_BODY
    //                    TYPE_OP type=kotlin.Unit origin=IMPLICIT_COERCION_TO_UNIT typeOperand=kotlin.Unit
    //                      BLOCK type=kotlin.Int origin=POSTFIX_INCR
    //                        VAR IR_TEMPORARY_VARIABLE name:tmp0 type:kotlin.Int [val]
    //                          GET_VAR 'var i: kotlin.Int [var] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment' type=kotlin.Int origin=POSTFIX_INCR
    //                        SET_VAR 'var i: kotlin.Int [var] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment' type=kotlin.Unit origin=POSTFIX_INCR
    //                          CALL 'public final fun inc (): kotlin.Int [operator] declared in kotlin.Int' type=kotlin.Int origin=POSTFIX_INCR
    //                            $this: GET_VAR 'val tmp0: kotlin.Int [val] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment.<anonymous>' type=kotlin.Int origin=null
    //                        GET_VAR 'val tmp0: kotlin.Int [val] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment.<anonymous>' type=kotlin.Int origin=null

    // eventHandler: FUN_EXPR type=kotlin.Function1<@[ParameterName(name = 'np0')] kotlin.Int, kotlin.Unit> origin=LAMBDA
    //                    FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> (it:kotlin.Int) returnType:kotlin.Unit
    //                      VALUE_PARAMETER name:it index:0 type:kotlin.Int
    //                      BLOCK_BODY
    //                        TYPE_OP type=kotlin.Unit origin=IMPLICIT_COERCION_TO_UNIT typeOperand=kotlin.Unit
    //                          BLOCK type=kotlin.Int origin=POSTFIX_INCR
    //                            VAR IR_TEMPORARY_VARIABLE name:tmp0 type:kotlin.Int [val]
    //                              CALL 'public final fun <get-i> (): kotlin.Int declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=kotlin.Int origin=GET_PROPERTY
    //                                $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                            COMPOSITE type=kotlin.Unit origin=null
    //                              VAR IR_TEMPORARY_VARIABLE name:tmp0 type:kotlin.Int [val]
    //                                GET_FIELD 'FIELD PROPERTY_BACKING_FIELD name:i type:kotlin.Int visibility:private' type=kotlin.Int origin=null
    //                                  receiver: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                              CALL 'public final fun set-i (set-?: kotlin.Int): kotlin.Unit declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=kotlin.Int origin=null
    //                                $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                                set-?: CALL 'public final fun inc (): kotlin.Int [operator] declared in kotlin.Int' type=kotlin.Int origin=POSTFIX_INCR
    //                                  $this: GET_VAR 'val tmp0: kotlin.Int [val] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment.<anonymous>' type=kotlin.Int origin=null
    //                              CALL 'public open fun ruiInvalidate0 (mask: kotlin.Int): kotlin.Unit declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=kotlin.Unit origin=null
    //                                $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                                mask: CONST Int type=kotlin.Int value=1
    //                              CALL 'public final fun println (message: kotlin.Any?): kotlin.Unit [inline] declared in kotlin.io.ConsoleKt' type=kotlin.Unit origin=null
    //                                message: STRING_CONCATENATION type=kotlin.String
    //                                  CONST String type=kotlin.String value="[RuiEventHandlerFragment       ]  state change          |"
    //                                  CONST String type=kotlin.String value=" i: "
    //                                  GET_VAR 'val tmp0: kotlin.Int [val] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment.<anonymous>' type=kotlin.Int origin=null
    //                                  CONST String type=kotlin.String value=" ⇢ "
    //                                  GET_FIELD 'FIELD PROPERTY_BACKING_FIELD name:i type:kotlin.Int visibility:private' type=kotlin.Int origin=null
    //                                    receiver: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null
    //                            GET_VAR 'val tmp0: kotlin.Int [val] declared in hu.simplexion.rui.kotlin.plugin.run.gen.eventHandlerFragment.<anonymous>' type=kotlin.Int origin=null
    //                        CALL 'public open fun ruiPatch (): kotlin.Unit declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=kotlin.Unit origin=null
    //                          $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiEventHandlerFragment origin=null


    fun IrBlockBuilder.traceStateChangeBefore(stateVariable: RuiStateVariable): IrVariable? {
        if (!ruiContext.withTrace) return null

        return irTemporary(irTraceGet(stateVariable, ruiClass.builder.irThisReceiver()))
            .also { it.parent = currentFunction!!.irElement as IrFunction }
    }

    fun IrBlockBuilder.traceStateChangeAfter(stateVariable: RuiStateVariable, traceData: IrVariable?) {
        if (traceData == null) return

        ruiClass.builder.irTrace("state change", listOf(
            irString("${stateVariable.name}:"),
            irGet(traceData),
            irString(" ⇢ "),
            irTraceGet(stateVariable, ruiClass.builder.irThisReceiver())
        ))
    }

    fun IrBlockBuilder.irTraceGet(stateVariable: RuiStateVariable, receiver: IrExpression): IrExpression =
        stateVariable.builder.irProperty.backingField
            ?.let { irGetField(receiver, it) }
            ?: irString("?")
}