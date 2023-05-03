/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.run

import hu.simplexion.rui.kotlin.plugin.RuiTest
import hu.simplexion.rui.kotlin.plugin.RuiTestDumpResult
import hu.simplexion.rui.kotlin.plugin.RuiTestResult
import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.rui
import hu.simplexion.rui.runtime.testing.T1

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
[ RuiRoot                        ]  init                  |  
[ RuiIfWithoutElse               ]  init                  |  i: 10
[ RuiRoot                        ]  create                |  
[ RuiIfWithoutElse               ]  create                |  
[ RuiRoot                        ]  mount                 |  
[ RuiIfWithoutElse               ]  mount                 |  
""".trimIndent()

