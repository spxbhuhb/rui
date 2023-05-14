/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.builders

import hu.simplexion.rui.kotlin.plugin.model.RuiHigherOrderArgument
import hu.simplexion.rui.kotlin.plugin.model.RuiHigherOrderCall

class RuiHigherOrderCallBuilder(
    ruiClassBuilder: RuiClassBuilder,
    ruiHigherOrderCall: RuiHigherOrderCall
) : RuiCallBuilder(
    ruiClassBuilder,
    ruiHigherOrderCall
) {
    var callSiteDependencyMask = 0L

    override fun buildDeclarations() {
        tryBuild(ruiCall.irCall) {

            symbolMap = ruiContext.ruiSymbolMap.getSymbolMap(ruiCall.targetRuiClass)

            callSiteDependencyMask = calcCallSiteDependencyMask() // dependency mask for the original call site

            buildHigherOrderArguments() // everything that belongs to the parameter functions
            buildExternalPatch() // external patch for the higher order component
        }
    }

    fun buildHigherOrderArguments() {
        for (argument in ruiCall.valueArguments) {
            if (argument is RuiHigherOrderArgument) {
                argument.builder.buildDeclarations()
            }
        }
    }

}