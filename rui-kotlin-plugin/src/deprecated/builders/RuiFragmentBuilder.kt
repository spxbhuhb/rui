/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.transform.builders

import org.jetbrains.kotlin.ir.expressions.IrExpression

@Deprecated("move to IR-RUM-AIR-IR")
interface RuiFragmentBuilder : RuiBuilderWithSymbolMap {

    /**
     * Add functions and properties used by this fragment. These depend on the
     * fragment type. External patch, select for branches, builders for loops and
     * higher-order functions etc.
     */
    fun buildDeclarations() {
        throw NotImplementedError()
    }

    /**
     * Create a new instance of the fragment. Called from `RumClass.build` after
     * all classes in the IR ModuleFragment are transformed into RumClass. This
     * ensures that references to other Rui classes can be resolved properly.
     */
    fun irNewInstance(): IrExpression {
        throw NotImplementedError()
    }

}