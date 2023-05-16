/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.util

import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumClass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.name.Name
import java.util.*

fun Name.isSynthetic() = identifier.startsWith('$') || identifier.endsWith('$')

fun String.capitalizeFirstChar() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

class RuiCompilationException(
    val error: ErrorsRui.RuiIrError,
    var rumClass: RumClass? = null,
    var irElement: IrElement? = null,
    val additionalInfo: String = ""
) : Exception() {

    var reported = false

    init {
        report()
    }

    fun report() {
        if (reported) return
        rumClass?.let { c ->
            irElement?.let { e ->
                error.report(c, e, additionalInfo)
                reported = true
            }
        }
    }
}