/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.ir2rum

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumDependencies
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.psi.KtModifierListOwner

class DependencyVisitor(
    private val rumClass: RumClass
) : RuiAnnotationBasedExtension, IrElementVisitorVoid {

    var dependencies: RumDependencies = mutableListOf()

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        rumClass.ruiContext.annotations

    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }

    /**
     * State variable reads are calls to the getter.
     */
    override fun visitCall(expression: IrCall) {
        rumClass.stateVariables.firstOrNull { it.matches(expression.symbol) }?.let {
            dependencies += it.index
        }
        super.visitCall(expression)
    }
}