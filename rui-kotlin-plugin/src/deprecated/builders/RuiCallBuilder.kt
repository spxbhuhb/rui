/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import hu.simplexion.rui.kotlin.plugin.ir.*
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

@Deprecated("move to IR-RUM-AIR-IR")
open class RuiCallBuilder(
    override val ruiClassBuilder: RuiClassBuilder,
    val rumCall: RumCall
) : RuiFragmentBuilder {

    // we have to initialize this in build, after all other classes in the module are registered
    override lateinit var symbolMap: RuiClassSymbols

    lateinit var externalPatchBuilder: RuiExternalPatchBuilder

    override fun buildDeclarations() {
        tryBuild(rumCall.irCall) {
            symbolMap = ruiContext.ruiSymbolMap.getSymbolMap(rumCall.target)
            buildExternalPatch()
        }
    }

    fun buildExternalPatch() {
        RuiExternalPatchBuilder(
            ruiClassBuilder,
            rumCall.irCall.startOffset,
            rumCall.valueArguments,
            calcCallSiteDependencyMask(),
            symbolMap
        ).also {
            it.buildExternalPatch()
            externalPatchBuilder = it
        }
    }

    protected fun calcCallSiteDependencyMask(): Long {
        var mask = 0L
        for (argument in rumCall.valueArguments) {
            for (index in argument.dependencies) {
                mask = mask or (1L shl index)
            }
        }
        return mask
    }

    override fun irNewInstance(): IrExpression =
        IrConstructorCallImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            symbolMap.defaultType,
            symbolMap.primaryConstructor.symbol,
            typeArgumentsCount = 1, // bridge type
            constructorTypeArgumentsCount = 0,
            rumCall.valueArguments.size + RUI_FRAGMENT_ARGUMENT_COUNT // +3 = adapter + scope + external patch
        ).also { constructorCall ->

            constructorCall.putTypeArgument(RUI_FRAGMENT_TYPE_INDEX_BRIDGE, classBoundBridgeType.defaultType)

            constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_ADAPTER, ruiClassBuilder.adapterPropertyBuilder.irGetValue())
            constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_SCOPE, ruiClassBuilder.scopePropertyBuilder.irGetValue())
            //constructorCall.putValueArgument(RUI_FRAGMENT_ARGUMENT_INDEX_EXTERNAL_PATCH, externalPatchBuilder.irExternalPatchReference())

            rumCall.valueArguments.forEachIndexed { index, ruiExpression ->
                constructorCall.putValueArgument(index + RUI_FRAGMENT_ARGUMENT_COUNT, ruiExpression.irExpression)
            }
        }

}