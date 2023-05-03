/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package hu.simplexion.rui.kotlin.plugin.diagnostics

import hu.simplexion.rui.kotlin.plugin.diagnostics.ErrorsRui.RUI_ON_INLINE_FUNCTION
import hu.simplexion.rui.kotlin.plugin.util.RuiAnnotationBasedExtension
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

internal class RuiDeclarationChecker(
    private val annotations: List<String>,
    private val useIr: Boolean
) : DeclarationChecker, RuiAnnotationBasedExtension {

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> =
        annotations

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        when (descriptor) {
            is FunctionDescriptor -> checkFunction(declaration, descriptor, context)
        }

//        if (descriptor !is ClassDescriptor || declaration !is KtClass) return
//        if (descriptor.kind != ClassKind.CLASS) return
//        if (!descriptor.hasSpecialAnnotation(declaration)) return
//
//        if (descriptor.isInner) {
//            val diagnostic = if (useIr) SDCP_ON_INNER_CLASS_ERROR else SDCP_ON_INNER_CLASS
//            context.trace.report(diagnostic.on(declaration.reportTarget))
//        } else if (DescriptorUtils.isLocal(descriptor)) {
//            val diagnostic = if (useIr) SDCP_ON_LOCAL_CLASS_ERROR else SDCP_ON_LOCAL_CLASS
//            context.trace.report(diagnostic.on(declaration.reportTarget))
//        }
//
//        val superClass = descriptor.getSuperClassOrAny()
//        if (superClass.constructors.none { it.isNoArgConstructor() } && !superClass.hasSpecialAnnotation(declaration)) {
//            context.trace.report(NO_SDCP_FUNCION_IN_SUPERCLASS.on(declaration.reportTarget))
//        }
    }

    private fun checkFunction(declaration: KtDeclaration, descriptor: FunctionDescriptor, context: DeclarationCheckerContext) {
        if (!descriptor.hasSpecialAnnotation(declaration)) return

        if (descriptor.isInline) {
            context.trace.report(RUI_ON_INLINE_FUNCTION.on(declaration))
        }

    }


    private val KtClass.reportTarget: PsiElement
        get() = nameIdentifier ?: getClassOrInterfaceKeyword() ?: this

    private fun ConstructorDescriptor.isNoArgConstructor(): Boolean =
        valueParameters.all(ValueParameterDescriptor::declaresDefaultValue)

}
