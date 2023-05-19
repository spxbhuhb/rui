package hu.simplexion.rui.kotlin.plugin.ir.sir

import hu.simplexion.rui.kotlin.plugin.ir.rum.RumForLoop
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression

class SirForLoop(
    override val rumElement: RumForLoop,
    override val externalPatch: IrSimpleFunction,
    override val builder: IrSimpleFunction,
    val iterator: IrExpression,
    val loopVariable: IrDeclaration,
    val implicitExternalPatch: IrSimpleFunction,
    val implicitBuilder: IrSimpleFunction
) : SirRenderingStatement