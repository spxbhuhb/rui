/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package hu.simplexion.rui.kotlin.plugin.diagnostics

import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap

object DefaultErrorMessagesRui : DefaultErrorMessages.Extension {

    private val MAP = DiagnosticFactoryToRendererMap("AnnotationProcessing")
    override fun getMap() = MAP

    init {
        MAP.put(ErrorsRui.RUI_ON_INLINE_FUNCTION, "Rui annotation is not allowed on inline functions.")
    }
}
