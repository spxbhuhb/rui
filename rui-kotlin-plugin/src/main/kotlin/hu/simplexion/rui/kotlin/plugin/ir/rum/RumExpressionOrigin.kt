/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum

enum class RumExpressionOrigin {
    VALUE_ARGUMENT,
    HIGHER_ORDER_ARGUMENT,
    BRANCH_CONDITION,
    BRANCH_RESULT,
    FOR_LOOP_CONDITION,
    FOR_LOOP_BODY,
}
