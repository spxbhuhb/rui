/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.DumpRumTreeVisitor
import hu.simplexion.rui.kotlin.plugin.ir.rum.visitors.RumElementVisitor

interface RumElement {
    fun <R, D> accept(visitor: RumElementVisitor<R, D>, data: D): R

    fun <D> acceptChildren(visitor: RumElementVisitor<Unit, D>, data: D)

    fun dump(): String {
        val out = StringBuilder()
        this.accept(DumpRumTreeVisitor(out), null)
        return out.toString()
    }

}