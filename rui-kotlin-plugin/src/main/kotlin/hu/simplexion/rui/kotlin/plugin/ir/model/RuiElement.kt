/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.model

import hu.simplexion.rui.kotlin.plugin.ir.util.DumpRuiTreeVisitor
import hu.simplexion.rui.kotlin.plugin.ir.util.RuiElementVisitor

interface RuiElement {
    fun <R, D> accept(visitor: RuiElementVisitor<R, D>, data: D): R

    fun <D> acceptChildren(visitor: RuiElementVisitor<Unit, D>, data: D)

    fun dump(): String {
        val out = StringBuilder()
        this.accept(DumpRuiTreeVisitor(out), null)
        return out.toString()
    }

}