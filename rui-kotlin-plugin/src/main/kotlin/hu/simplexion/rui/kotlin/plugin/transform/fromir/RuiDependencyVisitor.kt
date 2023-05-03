/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.fromir

import hu.simplexion.rui.kotlin.plugin.model.RuiClass
import hu.simplexion.rui.kotlin.plugin.model.RuiStateVariable
import hu.simplexion.rui.kotlin.plugin.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.psi.KtModifierListOwner

class RuiDependencyVisitor(
    private val ruiClass: RuiClass
) : RuiAnnotationBasedExtension, IrElementVisitorVoid {

    var dependencies = mutableListOf<RuiStateVariable>()

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        ruiClass.ruiContext.annotations

    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }

    /**
     * State variable reads are calls to the getter.
     */
    override fun visitCall(expression: IrCall) {
        ruiClass.stateVariableByGetterOrNull(expression.symbol)?.let {
            dependencies += it
        }
        super.visitCall(expression)
    }
}