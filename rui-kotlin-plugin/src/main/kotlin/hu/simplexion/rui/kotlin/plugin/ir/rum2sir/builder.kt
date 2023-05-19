package hu.simplexion.rui.kotlin.plugin.ir.rum2sir

import hu.simplexion.rui.kotlin.plugin.ir.RUI_BUILDER
import hu.simplexion.rui.kotlin.plugin.ir.util.ClassBoundIrBuilder
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.name.Name

/**
 * Defines a function (NNN = [startOffset]):
 *
 * ```kotlin
 * fun ruiBuilderNNN(startScope : RuiFragment<BT>) : RuiFragment<BT> {
 * }
 * ```
 */
context(ClassBoundIrBuilder)
fun builder(startOffset: Int): IrSimpleFunction =
    irFactory.buildFun {
        name = Name.identifier("$RUI_BUILDER$startOffset")
        returnType = classBoundFragmentType
        modality = Modality.OPEN
    }.also { function ->

        function.addDispatchReceiver {
            type = irClass.typeWith(irClass.typeParameters.first().defaultType)
        }

        function.addValueParameter {
            name = Name.identifier("startScope")
            type = classBoundFragmentType
        }

        function.parent = irClass
        irClass.declarations += function
    }
