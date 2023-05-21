/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration

class RumDeclaration(
    val rumClass: RumClass,
    val irDeclaration: IrDeclaration,
    val origin: RumDeclarationOrigin,
    val dependencies: RumDependencies
) : RumElement {

    override fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R =
        visitor.visitDeclaration(this, data)

    override fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D) = Unit

}