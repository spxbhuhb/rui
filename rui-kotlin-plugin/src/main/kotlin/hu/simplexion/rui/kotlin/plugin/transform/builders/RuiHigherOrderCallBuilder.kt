/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.transform.builders

import hu.simplexion.rui.kotlin.plugin.model.RuiHigherOrderCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

class RuiHigherOrderCallBuilder(
    ruiClassBuilder: RuiClassBuilder,
    ruiHigherOrderCall: RuiHigherOrderCall
) : RuiCallBuilder(
    ruiClassBuilder,
    ruiHigherOrderCall
) {

    override fun buildDeclarations() {
        super.buildDeclarations()
        tryBuild(ruiCall.irCall) {
            // buildExternalPatchForImplicit()
            // buildBuilderForImplicit()
        }
    }

    override fun irNewInstance(): IrExpression {
        return super.irNewInstance()
    }

}