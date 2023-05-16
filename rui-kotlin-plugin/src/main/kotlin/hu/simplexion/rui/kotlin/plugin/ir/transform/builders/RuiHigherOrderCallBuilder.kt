/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumHigherOrderArgument
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumHigherOrderCall

class RuiHigherOrderCallBuilder(
    ruiClassBuilder: RuiClassBuilder,
    ruiHigherOrderCall: RumHigherOrderCall
) : RuiCallBuilder(
    ruiClassBuilder,
    ruiHigherOrderCall
) {
    var callSiteDependencyMask = 0L

    override fun buildDeclarations() {
        tryBuild(rumCall.irCall) {

            symbolMap = ruiContext.ruiSymbolMap.getSymbolMap(rumCall.target)

            callSiteDependencyMask = calcCallSiteDependencyMask() // dependency mask for the original call site

            buildHigherOrderArguments() // everything that belongs to the parameter functions
            buildExternalPatch() // external patch for the higher order component
        }
    }

    fun buildHigherOrderArguments() {
        for (argument in rumCall.valueArguments) {
            if (argument is RumHigherOrderArgument) {
                argument.builder.buildDeclarations()
            }
        }
    }

}