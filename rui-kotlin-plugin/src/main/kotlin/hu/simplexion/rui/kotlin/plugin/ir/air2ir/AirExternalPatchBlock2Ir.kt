package hu.simplexion.rui.kotlin.plugin.ir.air2ir

import hu.simplexion.rui.kotlin.plugin.ir.ClassBoundIrBuilder
import hu.simplexion.rui.kotlin.plugin.ir.air.AirExternalPatchBlock
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody

/**
 * External patch generation for blocks. This is just an empty function as blocks do not have
 * state, therefore there is nothing to patch. Blocks call the external patch of the fragments
 * they contain from `patch`.
 */
class AirExternalPatchBlock2Ir(
    parent: ClassBoundIrBuilder,
    val externalPatch: AirExternalPatchBlock
) : ClassBoundIrBuilder(parent) {

    fun toIr() {
        externalPatch.irFunction.body = DeclarationIrBuilder(irContext, externalPatch.irFunction.symbol).irBlockBody { }
    }

}
