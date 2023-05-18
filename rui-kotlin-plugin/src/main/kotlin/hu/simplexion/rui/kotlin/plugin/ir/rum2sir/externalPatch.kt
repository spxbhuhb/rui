package hu.simplexion.rui.kotlin.plugin.ir.rum2sir

import hu.simplexion.rui.kotlin.plugin.ir.RUI_EXTERNAL_PATCH_OF_CHILD
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
 * Defines an `externalPatch${postfix}` function where postfix is typically
 * the call site offset.
 */
context(ClassBoundIrBuilder)
fun externalPatch(postfix: String): IrSimpleFunction =
    irFactory.buildFun {
        name = Name.identifier("$RUI_EXTERNAL_PATCH_OF_CHILD$postfix")
        returnType = irBuiltIns.longType
        modality = Modality.OPEN
    }.also { function ->

        function.addDispatchReceiver {
            type = irClass.typeWith(irClass.typeParameters.first().defaultType)
        }

        function.addValueParameter {
            name = Name.identifier("it")
            type = classBoundFragmentType
        }

        function.addValueParameter {
            name = Name.identifier("scopeMask")
            type = irBuiltIns.longType
        }

        function.parent = irClass
        irClass.declarations += function
    }
