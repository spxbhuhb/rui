/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.ir2rum

import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumBoundary
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.psi.KtModifierListOwner

/**
 * Finds the boundary between state definition and rendering parts of a RUI function.
 * The boundary is the `startOffset` of the first rendering statement.
 * Entry point is [findBoundary].
 */
class BoundaryVisitor(
    private val ruiContext: RuiPluginContext
) : RuiAnnotationBasedExtension, IrElementVisitorVoid {

    var found: Boolean = false

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        ruiContext.annotations

    fun findBoundary(declaration: IrFunction): RumBoundary {

        declaration.body?.statements?.let { statements ->

            statements.forEachIndexed { index, irStatement ->
                irStatement.acceptVoid(this)
                if (found) return RumBoundary(irStatement.startOffset, index)
            }

            return RumBoundary(declaration.endOffset, statements.size)
        }

        throw IllegalStateException("function has no body (maybe it's an expression function)")
    }

    override fun visitElement(element: IrElement) {
        if (found) return
        element.acceptChildren(this, null)
    }

    override fun visitCall(expression: IrCall) {
        if (expression.symbol.owner.isAnnotatedWithRui()) {
            found = true
        } else {
            super.visitCall(expression)
        }
    }
}
