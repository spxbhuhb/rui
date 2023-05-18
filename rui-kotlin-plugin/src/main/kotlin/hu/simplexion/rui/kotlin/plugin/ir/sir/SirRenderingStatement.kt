package hu.simplexion.rui.kotlin.plugin.ir.sir

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

open class SirRenderingStatement(
    val newInstance: IrConstructorCall
) : SirElement {
}