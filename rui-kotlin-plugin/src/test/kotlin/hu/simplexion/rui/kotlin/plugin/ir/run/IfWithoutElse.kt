/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.run

import hu.simplexion.rui.kotlin.plugin.ir.RuiTest
import hu.simplexion.rui.kotlin.plugin.ir.RuiTestDumpResult
import hu.simplexion.rui.kotlin.plugin.ir.RuiTestResult
import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.rui
import hu.simplexion.rui.runtime.testing.T1

//  factories: VARARG
//  factories: VARARG
//    type=kotlin.Array<out kotlin.Function0<hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode>>>
//    type=kotlin.Array<hu.simplexion.rui.runtime.RuiFragment<BT of hu.simplexion.rui.kotlin.plugin.run.gen.RuiIfWithoutElse>>
//    varargElementType=kotlin.Function0<hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode>>
//    varargElementType=hu.simplexion.rui.runtime.RuiFragment<BT of hu.simplexion.rui.kotlin.plugin.run.gen.RuiIfWithoutElse>

//    FUNCTION_REFERENCE 'public final fun ruiBranch0 (): hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode> declared in hu.simplexion.rui.kotlin.plugin.adhoc.Branch' type=kotlin.reflect.KFunction0<hu.simplexion.rui.runtime.RuiFragment<hu.simplexion.rui.runtime.testing.TestNode>> origin=null reflectionTarget=<same>
//    FUNCTION_REFERENCE 'local final fun ruiBranch1418 (): hu.simplexion.rui.runtime.RuiFragment<BT of hu.simplexion.rui.kotlin.plugin.run.gen.RuiIfWithoutElse> declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiIfWithoutElse' type=kotlin.Function0<hu.simplexion.rui.runtime.RuiFragment<BT of hu.simplexion.rui.kotlin.plugin.run.gen.RuiIfWithoutElse>> origin=null reflectionTarget=<same>

//      $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.adhoc.Branch declared in hu.simplexion.rui.kotlin.plugin.adhoc.Branch' type=hu.simplexion.rui.kotlin.plugin.adhoc.Branch origin=null
//      $this: GET_VAR '<this>: hu.simplexion.rui.kotlin.plugin.run.gen.RuiIfWithoutElse declared in hu.simplexion.rui.kotlin.plugin.run.gen.RuiIfWithoutElse' type=hu.simplexion.rui.kotlin.plugin.run.gen.RuiIfWithoutElse origin=null

@RuiTest
@RuiTestDumpResult
fun branchIfWithoutElse() {
    rui {
        ifWithoutElse(10)
    }
}

@Rui
fun ifWithoutElse(i: Int) {
    if (i == 1) T1(i)
}

@RuiTestResult
fun branchIfWithoutElseResult(): String = """
[ RuiRoot                        ]  init                  |  ruiScope: null
[ RuiIfWithoutElse               ]  init                  |  ruiScope: null i: 10
[ RuiRoot                        ]  create                |  
[ RuiIfWithoutElse               ]  create                |  
[ RuiRoot                        ]  mount                 |  
[ RuiIfWithoutElse               ]  mount                 |  
""".trimIndent()

