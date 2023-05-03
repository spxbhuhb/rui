/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.util

import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui
import hu.simplexion.rui.kotlin.plugin.model.RuiClass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.name.Name

fun Name.isSynthetic() = identifier.startsWith('$') || identifier.endsWith('$')

class RuiCompilationException(
    val error: ErrorsRui.RuiIrError,
    var ruiClass: RuiClass? = null,
    var irElement: IrElement? = null,
    val additionalInfo: String = ""
) : Exception() {

    var reported = false

    init {
        report()
    }

    fun report() {
        if (reported) return
        ruiClass?.let { c ->
            irElement?.let { e ->
                error.report(c, e, additionalInfo)
                reported = true
            }
        }
    }
}