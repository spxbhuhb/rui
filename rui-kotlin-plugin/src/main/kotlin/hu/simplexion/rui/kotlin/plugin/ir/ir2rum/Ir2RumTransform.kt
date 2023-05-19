/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.ir2rum

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RIU_IR_RENDERING_NON_RUI_CALL
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_INVALID_RENDERING_STATEMENT
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui.RUI_IR_RENDERING_INVALID_DECLARATION
import hu.simplexion.rui.kotlin.plugin.ir.rum.*
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.psi.KtModifierListOwner

/**
 * Transforms an original function into a [RumClass]. This is a somewhat complex transformation.
 *
 * Calls [StateTransform] to:
 *
 *   - convert function parameters into [RumExternalStateVariable] instances
 *   - convert function variables are into [RumInternalStateVariable] instances
 *
 * Transforms IR structures such as loops, branches and calls into [RumBlock] instances.
 * The type of the block corresponds with the language construct:
 *
 * - code block: [RumBlock]
 * - `for` : [RumForLoop]
 * - `if`, `when`: [RumWhen]
 * - function call: [RumCall]
 * - higher order function call: [RumHigherOrderCall]
 *
 * Calls [DependencyVisitor] to build dependencies for each block.
 */
class Ir2RumTransform(
    val ruiContext: RuiPluginContext,
    val irFunction: IrFunction,
    val skipParameters: Int
) : RuiAnnotationBasedExtension {

    lateinit var rumClass: RumClass

    var blockIndex = 0
        get() = field++

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        rumClass.ruiContext.annotations

    fun IrElement.dependencies(): List<RumStateVariable> {
        val visitor = DependencyVisitor(rumClass)
        accept(visitor, null)
        return visitor.dependencies
    }

    fun transform(): RumClass {
        rumClass = RumClass(ruiContext, irFunction)

        StateTransform(ruiContext, rumClass, skipParameters).transform()

        transformRoot()

        return rumClass
    }

    fun transformRoot() {
        val statements = rumClass.originalStatements

        val irBlock = IrBlockImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, ruiContext.irContext.irBuiltIns.unitType)
        irBlock.statements.addAll(statements.subList(rumClass.boundary, statements.size))

        // if this is a single statement, we don't need the surrounding block
        // TODO check if this is the right place for the optimization
        rumClass.rootBlock = transformBlock(irBlock).let {
            if (it.statements.size == 1) {
                it.statements[0]
            } else {
                it
            }
        }
    }

    fun transformBlock(expression: IrBlock): RumBlock {

        val rumBlock = RumBlock(rumClass, blockIndex, expression)

        expression.statements.forEach { statement ->
            when (statement) {
                is IrBlock -> {
                    when (statement.origin) {
                        IrStatementOrigin.FOR_LOOP -> transformForLoop(statement)
                        IrStatementOrigin.WHEN -> transformWhen(statement)
                        else -> RUI_IR_INVALID_RENDERING_STATEMENT.report(rumClass, statement)
                    }
                }

                is IrCall -> transformCall(statement)
                is IrWhen -> transformWhen(statement)
                else -> RUI_IR_INVALID_RENDERING_STATEMENT.report(rumClass, statement)

            }?.let {
                rumBlock.statements += it
            }
        }

        return rumBlock
    }

    // ---------------------------------------------------------------------------
    // For Loop
    // ---------------------------------------------------------------------------

    fun transformForLoop(statement: IrBlock): RumForLoop? {

        // BLOCK type=kotlin.Unit origin=FOR_LOOP
        //          VAR FOR_LOOP_ITERATOR name:tmp0_iterator type:kotlin.collections.IntIterator [val]
        //            CALL 'public open fun iterator (): kotlin.collections.IntIterator [fake_override,operator] declared in kotlin.ranges.IntRange' type=kotlin.collections.IntIterator origin=FOR_LOOP_ITERATOR
        //              $this: CALL 'public final fun rangeTo (other: kotlin.Int): kotlin.ranges.IntRange [operator] declared in kotlin.Int' type=kotlin.ranges.IntRange origin=RANGE
        //                $this: CONST Int type=kotlin.Int value=0
        //                other: CONST Int type=kotlin.Int value=10
        //          WHILE label=null origin=FOR_LOOP_INNER_WHILE
        //            condition: CALL 'public abstract fun hasNext (): kotlin.Boolean [fake_override,operator] declared in kotlin.collections.IntIterator' type=kotlin.Boolean origin=FOR_LOOP_HAS_NEXT
        //              $this: GET_VAR 'val tmp0_iterator: kotlin.collections.IntIterator [val] declared in hu.simplexion.rui.kotlin.plugin.successes.Basic' type=kotlin.collections.IntIterator origin=null
        //            body: BLOCK type=kotlin.Unit origin=FOR_LOOP_INNER_WHILE
        //              VAR FOR_LOOP_VARIABLE name:i type:kotlin.Int [val]
        //                CALL 'public final fun next (): kotlin.Int [operator] declared in kotlin.collections.IntIterator' type=kotlin.Int origin=FOR_LOOP_NEXT
        //                  $this: GET_VAR 'val tmp0_iterator: kotlin.collections.IntIterator [val] declared in hu.simplexion.rui.kotlin.plugin.successes.Basic' type=kotlin.collections.IntIterator origin=null
        //              BLOCK type=kotlin.Unit origin=null
        //                CALL 'public final fun P1 (p0: kotlin.Int): kotlin.Unit declared in hu.simplexion.rui.kotlin.plugin' type=kotlin.Unit origin=null
        //                  p0: GET_VAR 'val i: kotlin.Int [val] declared in hu.simplexion.rui.kotlin.plugin.successes.Basic' type=kotlin.Int origin=null

        // TODO convert checks into non-exception throwing, but contracting functions
        check(statement.statements.size == 2)

        val irIterator = statement.statements[0]
        val loop = statement.statements[1]

        check(irIterator is IrValueDeclaration && irIterator.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR)
        check(loop is IrWhileLoop && loop.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE)

        val condition = transformExpression(loop.condition, RumExpressionOrigin.FOR_LOOP_CONDITION)

        val body = loop.body

        check(body is IrBlock && body.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE)
        check(body.statements.size == 2)

        val irLoopVariable = body.statements[0]
        val block = body.statements[1]

        check(irLoopVariable is IrVariable)
        check(block is IrBlock && block.origin == null)

        val iterator = transformDeclaration(irIterator, RumDeclarationOrigin.FOR_LOOP_ITERATOR) ?: return null
        val loopVariable = transformDeclaration(irLoopVariable, RumDeclarationOrigin.FOR_LOOP_VARIABLE) ?: return null

        val rendering = transformRenderingExpression(block)
            ?: return null

        return RumForLoop(
            rumClass, blockIndex, statement,
            iterator,
            condition,
            loopVariable,
            rendering
        )
    }

    fun transformDeclaration(declaration: IrDeclaration, origin: RumDeclarationOrigin): RumDeclaration? =
        when (declaration) {
            is IrValueDeclaration -> RumDeclaration(rumClass, declaration, origin, declaration.dependencies())
            else -> RUI_IR_RENDERING_INVALID_DECLARATION.report(rumClass, declaration)
        }

    // ---------------------------------------------------------------------------
    // Call
    // ---------------------------------------------------------------------------

    fun transformCall(statement: IrCall): RumCall? {

        if (!statement.symbol.owner.isAnnotatedWithRui()) {
            return RIU_IR_RENDERING_NON_RUI_CALL.report(rumClass, statement)
        }

        return if (statement.isHigherOrder) {
            transformHigherOrderCall(statement)
        } else {
            transformSimpleCall(statement)
        }
    }

    val IrCall.isHigherOrder: Boolean
        get() {
            // TODO check if caching for higher order function symbols has positive impact on compilation performance
            // TODO default parameter values for higher order calls
            for (index in 0 until valueArgumentsCount) {
                val va = getValueArgument(index) ?: continue
                if (va !is IrFunctionExpression) continue // this is just a speed to avoid the line below when possible
                if (this.symbol.owner.valueParameters.firstOrNull { it.isAnnotatedWithRui() } == null) continue
                return true
            }
            return false
        }

    fun transformSimpleCall(statement: IrCall): RumCall {
        val rumCall = RumCall(rumClass, blockIndex, statement)

        for (index in 0 until statement.valueArgumentsCount) {
            val expression = statement.getValueArgument(index) ?: continue
            rumCall.valueArguments += transformValueArgument(index, expression)
        }

        return rumCall
    }

    fun transformValueArgument(index: Int, expression: IrExpression): RumExpression {
        return RumValueArgument(rumClass, index, expression, expression.dependencies())
    }

    fun transformHigherOrderCall(irCall: IrCall): RumHigherOrderCall {
        val ruiHigherOrderCall = RumHigherOrderCall(rumClass, blockIndex, irCall)

        val calleeArguments = irCall.symbol.owner.valueParameters // arguments of the higher order function

        for (index in 0 until irCall.valueArgumentsCount) {
            val expression = irCall.getValueArgument(index) ?: continue // TODO handle parameter default values

            ruiHigherOrderCall.valueArguments +=
                if (calleeArguments[index].isAnnotatedWithRui() && expression is IrFunctionExpression) {
                    transformHigherOrderArgument(index, expression)
                } else {
                    transformValueArgument(index, expression)
                }
        }

        return ruiHigherOrderCall
    }

    /**
     * This is actually a very complicated and costly transformation as it calls [Ir2RumTransform]
     * recursively as long as there are higher order function calls inside the higher order function call.
     */
    fun transformHigherOrderArgument(index: Int, expression: IrFunctionExpression): RumExpression =
        RumHigherOrderArgument(
            rumClass,
            index,
            expression,
            expression.dependencies(),
            Ir2RumTransform(ruiContext, expression.function, 0).transform()
        )

    // ---------------------------------------------------------------------------
    // When
    // ---------------------------------------------------------------------------

    /**
     * Transforms a `when` with a subject variable like:
     *
     * ```kotlin
     * when (b) {
     *   // ...
     * }
     * ```
     */
    fun transformWhen(statement: IrBlock): RumWhen? {
        // TODO convert checks into non-exception throwing, but contracting functions
        check(statement.statements.size == 2)

        val subject = statement.statements[0]
        val evaluation = statement.statements[1]

        check(subject is IrVariable)
        check(evaluation is IrWhen && evaluation.origin == IrStatementOrigin.WHEN)

        return transformWhen(evaluation, subject)
    }

    fun transformWhen(statement: IrWhen, subject: IrVariable? = null): RumWhen? {
        val ruiWhen = RumWhen(rumClass, blockIndex, subject, statement)

        statement.branches.forEach { irBranch ->
            ruiWhen.branches += transformBranch(irBranch) ?: return null
        }

        return ruiWhen
    }

    fun transformBranch(branch: IrBranch): RumBranch? {
        val rendering = transformRenderingExpression(branch.result)
            ?: return null

        return RumBranch(
            rumClass, blockIndex,
            branch,
            transformExpression(branch.condition, RumExpressionOrigin.BRANCH_CONDITION),
            rendering,
        )
    }

    fun transformExpression(expression: IrExpression, origin: RumExpressionOrigin): RumExpression {
        return RumExpression(rumClass, expression, origin, expression.dependencies())
    }

    fun transformRenderingExpression(expression: IrExpression): RumRenderingStatement? {
        return when (expression) {
            is IrCall -> transformCall(expression)
            is IrBlock -> transformBlock(expression)
            is IrWhen -> transformWhen(expression)
            else -> {
                RUI_IR_RENDERING_INVALID_DECLARATION.report(rumClass, expression)
                null
            }
        }
    }

}