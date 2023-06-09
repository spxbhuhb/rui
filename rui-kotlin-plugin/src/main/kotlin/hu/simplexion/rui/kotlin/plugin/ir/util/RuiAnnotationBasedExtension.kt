/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.util

import org.jetbrains.kotlin.extensions.AnnotationBasedExtension
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor

interface RuiAnnotationBasedExtension : AnnotationBasedExtension {

//    fun isRui(declaration: IrClass): Boolean =
//        declaration.kind == ClassKind.CLASS &&
//            declaration.isAnnotatedWithRui()
//
//    fun isRui(declaration: IrFunction): Boolean =
//        declaration.isAnnotatedWithRui()
//
//    fun isRui(symbol: IrSimpleFunctionSymbol): Boolean =
//        symbol.owner.isAnnotatedWithRui()
//
//    fun IrClass.isAnnotatedWithRui(): Boolean =
//        toIrBasedDescriptor().hasSpecialAnnotation(null)

    fun IrValueParameter.isAnnotatedWithRui(): Boolean =
        toIrBasedDescriptor().hasSpecialAnnotation(null)

    fun IrFunction.isAnnotatedWithRui(): Boolean =
        toIrBasedDescriptor().hasSpecialAnnotation(null)

}