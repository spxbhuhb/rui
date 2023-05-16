/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum.visitors

import hu.simplexion.rui.kotlin.plugin.ir.rum.*

interface RumElementVisitor<out R, in D> {
    fun visitElement(element: RumElement, data: D): R

    fun visitEntryPoint(rumEntryPoint: RumEntryPoint, data: D) = visitElement(rumEntryPoint, data)

    fun visitClass(rumClass: RumClass, data: D) = visitElement(rumClass, data)

    fun visitStateVariable(stateVariable: RumStateVariable, data: D) = visitElement(stateVariable, data)
    fun visitExternalStateVariable(stateVariable: RumExternalStateVariable, data: D) = visitStateVariable(stateVariable, data)
    fun visitInternalStateVariable(stateVariable: RumInternalStateVariable, data: D) = visitStateVariable(stateVariable, data)

    fun visitDirtyMask(dirtyMask: RumDirtyMask, data: D) = visitElement(dirtyMask, data)

    fun visitStatement(statement: RumStatement, data: D) = visitElement(statement, data)
    fun visitBlock(statement: RumBlock, data: D) = visitStatement(statement, data)
    fun visitCall(statement: RumCall, data: D) = visitStatement(statement, data)
    fun visitHigherOrderCall(statement: RumHigherOrderCall, data: D) = visitStatement(statement, data)
    fun visitWhen(statement: RumWhen, data: D) = visitStatement(statement, data)
    fun visitForLoop(statement: RumForLoop, data: D) = visitStatement(statement, data)

    fun visitExpression(expression: RumExpression, data: D) = visitElement(expression, data)
    fun visitValueArgument(valueArgument: RumValueArgument, data: D) = visitElement(valueArgument, data)
    fun visitHigherOrderArgument(higherOrderArgument: RumHigherOrderArgument, data: D) = visitElement(higherOrderArgument, data)

    fun visitDeclaration(declaration: RumDeclaration, data: D) = visitElement(declaration, data)

    fun visitBranch(branch: RumBranch, data: D) = visitElement(branch, data)

}