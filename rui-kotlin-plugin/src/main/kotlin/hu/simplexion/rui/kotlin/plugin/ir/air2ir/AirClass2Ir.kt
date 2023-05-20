package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.RUI_PATCH_ARGUMENT_INDEX_SCOPE_MASK
import hu.simplexion.rui.kotlin.plugin.ir.RuiPluginContext
import hu.simplexion.rui.kotlin.plugin.ir.air.AirClass
import hu.simplexion.rui.kotlin.plugin.ir.air.AirDirtyMask
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions

class AirClass2Ir(
    context: RuiPluginContext,
    airClass: AirClass
) : ClassBoundIrBuilder(context, airClass) {

    fun toIr(): IrClass {
        airClass.initializer.toIr()
        airClass.builder.toIr(this)

        patch()

        return irClass
    }

    fun IrAnonymousInitializer.toIr() {
        body = irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
        body.statements += airClass.rumElement.initializerStatements
        //body.statements += fragment.irSetField(<ircall builder>, <this>)

        // The initializer has to be the last, so it will be able to access all properties
        irClass.declarations += this
    }

    fun patch() {
        val patch = airClass.patch

        patch.body = DeclarationIrBuilder(irContext, patch.symbol).irBlockBody {
            // SOURCE  val extendedScopeMask = ruiFragment.ruiExternalPatch(ruiFragment, scopeMask)
            val extendedScopeMask = irCallExternalPatchFromPatch(patch)

            // SOURCE  if (extendedScopeMask != 0L) fragment.ruiPatch(extendedScopeMask)
            +irIf(
                irNotEqual(
                    irGet(extendedScopeMask),
                    irConst(0L)
                ),
                irCallOp(
                    airClass.builder.symbolMap.patch.symbol,
                    type = irBuiltIns.unitType,
                    dispatchReceiver = irGetFragment(patch),
                    argument = irGet(extendedScopeMask)
                )
            )

            // SOURCE  ruiDirty0 = 0L
            airClass.dirtyMasks.forEach { +it.irClear(patch.dispatchReceiverParameter!!) }
        }

    }


    /**
     * Call the external patch of `ruiFragment`. This is somewhat complex because the function
     * is stored in a variable.
     *
     * ```kotlin
     * ruiFragment.ruiExternalPatch(ruiFragment, scopeMask)
     * ```
     *
     * ```text
     * CALL 'public abstract fun invoke (p1: P1 of kotlin.Function1): R of kotlin.Function1 [operator] declared in kotlin.Function1' type=kotlin.Unit origin=INVOKE
     *   $this: CALL 'public abstract fun <get-ruiExternalPatch> (): kotlin.Function1<@[ParameterName(name = 'it')] hu.simplexion.rui.runtime.RuiFragment<BT of hu.simplexion.rui.runtime.RuiFragment>, kotlin.Unit> declared in hu.simplexion.rui.runtime.RuiFragment' type=kotlin.Function1<@[ParameterName(name = 'it')] hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode>, kotlin.Unit> origin=GET_PROPERTY
     *     $this: CALL 'public open fun <get-fragment> (): hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> declared in hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air' type=hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> origin=GET_PROPERTY
     *       $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air declared in hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air.ruiPatch' type=hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air origin=null
     *   p1: CALL 'public open fun <get-fragment> (): hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> declared in hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air' type=hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> origin=GET_PROPERTY
     *     $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air declared in hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air.ruiPatch' type=hu.simplexion.rui.kotlin.plugin.adhoc.RumBlock2Air origin=null
     * ```
     */
    fun IrBlockBodyBuilder.irCallExternalPatchFromPatch(patch: IrSimpleFunction): IrVariable {

        val function2Type = irBuiltIns.functionN(2)
        val invoke = function2Type.functions.first { it.name.identifier == "invoke" }.symbol

        val fragment = irTemporary(irGetFragment(patch))

        // returns with the extended scope mask
        return irTemporary(
            irCall(
                invoke,
                irBuiltIns.longType,
                valueArgumentsCount = 2,
                typeArgumentsCount = 0,
                origin = IrStatementOrigin.INVOKE
            ).apply {
                dispatchReceiver = irCallOp(
                    airClass.builder.symbolMap.externalPatchGetter.symbol,
                    function2Type.defaultType,
                    irGet(fragment)
                )
                putValueArgument(0, irGet(fragment))
                putValueArgument(1, irGet(patch.valueParameters[RUI_PATCH_ARGUMENT_INDEX_SCOPE_MASK]))
            }
        )
    }

    /**
     * Fetch the instance of this fragment. Dispatch receiver of the scope has to be the class itself.
     */
    fun IrBlockBodyBuilder.irGetFragment(scope: IrSimpleFunction): IrExpression {
        // FIXME check receiver logic when having deeper structures
        return irGet(
            airClass.fragment.backingField!!.type,
            IrGetValueImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, scope.dispatchReceiverParameter!!.symbol),
            airClass.fragment.getter!!.symbol
        )
    }

    fun AirDirtyMask.irClear(receiver: IrValueParameter): IrStatement =
        irProperty.irSetValue(
            irConst(0L),
            receiver = irGet(receiver)
        )

}

