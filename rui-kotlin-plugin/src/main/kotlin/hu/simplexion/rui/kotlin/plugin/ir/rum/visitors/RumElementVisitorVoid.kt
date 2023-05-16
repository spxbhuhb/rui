/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum.visitors

import hu.simplexion.rui.kotlin.plugin.ir.rum.*

interface RumElementVisitorVoid<out R> : RumElementVisitor<R, Nothing?> {

    fun visitElement(element: RumElement): R
    override fun visitElement(element: RumElement, data: Nothing?) = visitElement(element)

    fun visitEntryPoint(rumEntryPoint: RumEntryPoint) = visitElement(rumEntryPoint)
    override fun visitEntryPoint(rumEntryPoint: RumEntryPoint, data: Nothing?) = visitEntryPoint(rumEntryPoint)

    fun visitClass(rumClass: RumClass) = visitElement(rumClass)
    override fun visitClass(rumClass: RumClass, data: Nothing?) = visitClass(rumClass)

    fun visitStateVariable(stateVariable: RumStateVariable) = visitElement(stateVariable)
    override fun visitStateVariable(stateVariable: RumStateVariable, data: Nothing?) = visitStateVariable(stateVariable)

    fun visitExternalStateVariable(stateVariable: RumExternalStateVariable) = visitElement(stateVariable)
    override fun visitExternalStateVariable(stateVariable: RumExternalStateVariable, data: Nothing?) = visitExternalStateVariable(stateVariable)

    fun visitInternalStateVariable(stateVariable: RumInternalStateVariable) = visitElement(stateVariable)
    override fun visitInternalStateVariable(stateVariable: RumInternalStateVariable, data: Nothing?) = visitInternalStateVariable(stateVariable)

    fun visitDirtyMask(dirtyMask: RumDirtyMask) = visitElement(dirtyMask)
    override fun visitDirtyMask(dirtyMask: RumDirtyMask, data: Nothing?) = visitDirtyMask(dirtyMask)

    fun visitStatement(statement: RumStatement) = visitElement(statement)
    override fun visitStatement(statement: RumStatement, data: Nothing?) = visitStatement(statement)

    fun visitBlock(statement: RumBlock) = visitElement(statement)
    override fun visitBlock(statement: RumBlock, data: Nothing?) = visitBlock(statement)

    fun visitCall(statement: RumCall) = visitElement(statement)
    override fun visitCall(statement: RumCall, data: Nothing?) = visitCall(statement)

    fun visitHigherOrderCall(statement: RumHigherOrderCall) = visitElement(statement)
    override fun visitHigherOrderCall(statement: RumHigherOrderCall, data: Nothing?) = visitHigherOrderCall(statement)

    fun visitWhen(statement: RumWhen) = visitElement(statement)
    override fun visitWhen(statement: RumWhen, data: Nothing?) = visitWhen(statement)

    fun visitForLoop(statement: RumForLoop) = visitElement(statement)
    override fun visitForLoop(statement: RumForLoop, data: Nothing?) = visitForLoop(statement)

    fun visitExpression(expression: RumExpression) = visitElement(expression)
    override fun visitExpression(expression: RumExpression, data: Nothing?) = visitExpression(expression)

    fun visitValueArgument(valueArgument: RumValueArgument) = visitExpression(valueArgument)
    override fun visitValueArgument(valueArgument: RumValueArgument, data: Nothing?) = visitValueArgument(valueArgument)

    fun visitHigherOrderArgument(higherOrderArgument: RumHigherOrderArgument) = visitExpression(higherOrderArgument)
    override fun visitHigherOrderArgument(higherOrderArgument: RumHigherOrderArgument, data: Nothing?) = visitHigherOrderArgument(higherOrderArgument)

    fun visitDeclaration(declaration: RumDeclaration) = visitElement(declaration)
    override fun visitDeclaration(declaration: RumDeclaration, data: Nothing?) = visitDeclaration(declaration)

    fun visitBranch(branch: RumBranch) = visitElement(branch)
    override fun visitBranch(branch: RumBranch, data: Nothing?) = visitBranch(branch)

}