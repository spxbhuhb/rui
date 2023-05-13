/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.fromir

import hu.simplexion.rui.kotlin.plugin.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui.RIU_IR_RENDERING_NON_RUI_CALL
import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui.RUI_IR_INVALID_RENDERING_STATEMENT
import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui.RUI_IR_MISSING_EXPRESSION_ARGUMENT
import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui.RUI_IR_RENDERING_INVALID_DECLARATION
import hu.simplexion.rui.kotlin.plugin.model.*
import hu.simplexion.rui.kotlin.plugin.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.psi.KtModifierListOwner

/**
 * Transforms one function into a [RuiClass]. This is a somewhat complex transformation.
 *
 * Calls [RuiStateTransform] to:
 *
 *   - convert function parameters into [RuiExternalStateVariable] instances
 *   - convert function variables are into [RuiInternalStateVariable] instances
 *
 * Transforms IR structures such as loops, branches and calls into [RuiBlock] instances.
 * The type of the block corresponds with the language construct:
 *
 * - code block: [RuiBlock]
 * - `for` : [RuiForLoop]
 * - `if`, `when`: [RuiWhen]
 * - function call: [RuiCall]
 * - higher order function call: [RuiHigherOrderCall]
 *
 * Calls [RuiDependencyVisitor] to build dependencies for each block.
 */
class RuiFromIrTransform(
    val ruiContext: RuiPluginContext,
    val irFunction: IrFunction,
    val skipParameters: Int
) : RuiAnnotationBasedExtension {

    lateinit var ruiClass: RuiClass

    var blockIndex = 0
        get() = field ++

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        ruiClass.ruiContext.annotations

    fun IrElement.dependencies(): List<RuiStateVariable> {
        val visitor = RuiDependencyVisitor(ruiClass)
        accept(visitor, null)
        return visitor.dependencies
    }

    fun transform(): RuiClass {
        ruiClass = RuiClass(ruiContext, irFunction)

        RuiStateTransform(ruiContext, ruiClass, skipParameters).transform()

        transformRoot()

        return ruiClass
    }

    fun transformRoot() {
        val statements = ruiClass.originalStatements

        val irBlock = IrBlockImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, ruiContext.irContext.irBuiltIns.unitType)
        irBlock.statements.addAll(statements.subList(ruiClass.boundary, statements.size))

        // if this is a single statement, we don't need the surrounding block
        // TODO check if this is the right place for the optimization
        ruiClass.rootBlock = transformBlock(irBlock).let {
            if (it.statements.size == 1) {
                it.statements[0]
            } else {
                it
            }
        }
    }

    fun transformBlock(expression: IrBlock): RuiBlock {

        val ruiBlock = RuiBlock(ruiClass, blockIndex, expression)

        expression.statements.forEach { statement ->
            when (statement) {
                is IrBlock -> {
                    when (statement.origin) {
                        IrStatementOrigin.FOR_LOOP -> transformForLoop(statement)
                        IrStatementOrigin.WHEN -> transformWhen(statement)
                        else -> RUI_IR_INVALID_RENDERING_STATEMENT.report(ruiClass, statement)
                    }
                }

                is IrCall -> transformCall(statement)
                is IrWhen -> transformWhen(statement)
                else -> RUI_IR_INVALID_RENDERING_STATEMENT.report(ruiClass, statement)

            }?.let {
                ruiBlock.statements += it
            }
        }

        return ruiBlock
    }

    // ---------------------------------------------------------------------------
    // For Loop
    // ---------------------------------------------------------------------------

    fun transformForLoop(statement: IrBlock): RuiForLoop? {

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

        val condition = transformExpression(loop.condition, RuiExpressionOrigin.FOR_LOOP_CONDITION)

        val body = loop.body

        check(body is IrBlock && body.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE)
        check(body.statements.size == 2)

        val irLoopVariable = body.statements[0]
        val block = body.statements[1]

        check(irLoopVariable is IrVariable)
        check(block is IrBlock && block.origin == null)

        val iterator = transformDeclaration(irIterator, RuiDeclarationOrigin.FOR_LOOP_ITERATOR) ?: return null
        val loopVariable = transformDeclaration(irLoopVariable, RuiDeclarationOrigin.FOR_LOOP_VARIABLE) ?: return null

        val rendering = transformRenderingExpression(block)
            ?: return null

        return RuiForLoop(
            ruiClass, blockIndex, statement,
            iterator,
            condition,
            loopVariable,
            rendering
        )
    }

    fun transformDeclaration(declaration: IrDeclaration, origin: RuiDeclarationOrigin): RuiDeclaration? =
        when (declaration) {
            is IrValueDeclaration -> RuiDeclaration(ruiClass, declaration, origin, declaration.dependencies())
            else -> RUI_IR_RENDERING_INVALID_DECLARATION.report(ruiClass, declaration)
        }

    // ---------------------------------------------------------------------------
    // Call
    // ---------------------------------------------------------------------------

    fun transformCall(statement: IrCall): RuiCall? {

        if (! statement.symbol.owner.isAnnotatedWithRui()) {
            return RIU_IR_RENDERING_NON_RUI_CALL.report(ruiClass, statement)
        }

        return if (statement.isHigherOrder) {
            transformHigherOrderCall(statement)
        } else {
            transformSimpleCall(statement)
        }
    }

    fun IrCall.forValueArguments(process: (index: Int, expression: IrExpression) -> Unit) {
        for (index in 0 until valueArgumentsCount) {
            val expression = getValueArgument(index)
            if (expression == null) {
                RUI_IR_MISSING_EXPRESSION_ARGUMENT.report(ruiClass, this) // I think this should never happen
            } else {
                process(index, expression)
            }
        }
    }

    val IrCall.isHigherOrder: Boolean
        get() {
            var value = false
            forValueArguments { _, expression ->
                if (expression is IrFunctionExpression) {
                    if (this.symbol.owner.valueParameters.firstOrNull { it.isAnnotatedWithRui() } != null) {
                        // TODO check if caching for higher order function symbols has positive impact on compilation performance
                        value = true
                    }
                }
            }
            return value
        }

    fun transformSimpleCall(statement: IrCall): RuiCall {
        val ruiCall = RuiCall(ruiClass, blockIndex, statement)

        statement.forValueArguments { index, expression ->
            ruiCall.valueArguments += transformValueArgument(index, expression)
        }

        return ruiCall
    }

    fun transformHigherOrderCall(statement: IrCall): RuiHigherOrderCall {
        val ruiCall = RuiHigherOrderCall(ruiClass, blockIndex, statement)

        statement.forValueArguments { index, expression ->
            ruiCall.valueArguments += transformValueArgument(index, expression)
        }

        return ruiCall
    }

    fun transformValueArgument(index: Int, expression: IrExpression): RuiExpression {
        return RuiValueArgument(ruiClass, index, expression, expression.dependencies())
    }

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
    fun transformWhen(statement: IrBlock): RuiWhen? {
        // TODO convert checks into non-exception throwing, but contracting functions
        check(statement.statements.size == 2)

        val subject = statement.statements[0]
        val evaluation = statement.statements[1]

        check(subject is IrVariable)
        check(evaluation is IrWhen && evaluation.origin == IrStatementOrigin.WHEN)

        return transformWhen(evaluation, subject)
    }

    fun transformWhen(statement: IrWhen, subject: IrVariable? = null): RuiWhen? {
        val ruiWhen = RuiWhen(ruiClass, blockIndex, subject, statement)

        statement.branches.forEach { irBranch ->
            ruiWhen.branches += transformBranch(irBranch) ?: return null
        }

        return ruiWhen
    }

    fun transformBranch(branch: IrBranch): RuiBranch? {
        val rendering = transformRenderingExpression(branch.result)
            ?: return null

        return RuiBranch(
            ruiClass, blockIndex,
            branch,
            transformExpression(branch.condition, RuiExpressionOrigin.BRANCH_CONDITION),
            rendering,
        )
    }

    fun transformExpression(expression: IrExpression, origin: RuiExpressionOrigin): RuiExpression {
        return RuiExpression(ruiClass, expression, origin, expression.dependencies())
    }

    fun transformRenderingExpression(expression: IrExpression): RuiStatement? {
        return when (expression) {
            is IrCall -> transformCall(expression)
            is IrBlock -> transformBlock(expression)
            is IrWhen -> transformWhen(expression)
            else -> {
                RUI_IR_RENDERING_INVALID_DECLARATION.report(ruiClass, expression)
                null
            }
        }
    }

}